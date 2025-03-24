package use_cases.commands

import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parser.RDFSurfaceParseServiceImpl
import interface_adapters.services.theoremProver.TheoremProverRunnerServiceImpl
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import use_cases.commands.TransformQaError.MoreThanOneQuestionSurface
import use_cases.commands.TransformQaError.NoQuestionSurface
import use_cases.commands.subUseCase.AnswerTupleTransformationSuccess
import use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
import use_cases.commands.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import util.commandResult.*
import java.io.InputStream
import java.nio.file.Path

class TransformQaUseCase {

    operator fun invoke(
        inputStream: InputStream,
        optionId: Int,
        programName: String,
        reasoningTimeLimit: Long,
        outputPath: Path?,
        useRdfLists: Boolean,
        baseIri: IRI,
        configFile: Path,
        dEntailment: Boolean
    ): Flow<CommandStatus<Success, RootError>> = channelFlow {

        val rdfSurface = inputStream.reader().use { it.readText() }
        val parseResult = RDFSurfaceParseServiceImpl(useRdfLists).parseToEnd(rdfSurface, baseIri)
        val folFormula = parseResult
            .runOnSuccess { positiveSurface ->
                RdfSurfaceModelToTPTPModelUseCase(
                    defaultPositiveSurface = positiveSurface,
                    ignoreQuerySurfaces = false,
                    dEntailment = dEntailment
                )
            }
            .getOrElse {
                send(error(it))
                return@channelFlow
            }
            .joinToString(separator = System.lineSeparator()) { TPTPAnnotatedFormulaModelToStringUseCase(it) }

        val qSurfaces = parseResult.getOrElse { PositiveSurface() }.getQSurfaces()
        val qSurface = when {
            qSurfaces.isEmpty() -> {
                send(error(NoQuestionSurface))
                return@channelFlow
            }

            qSurfaces.size > 1 -> {
                send(error(MoreThanOneQuestionSurface))
                return@channelFlow
            }

            else -> qSurfaces.single()
        }

        outputPath?.let {
            FileService.createNewFile(
                path = it,
                content = folFormula
            )
        }

        val command = GetTheoremProverCommandUseCase(
            programName,
            optionId,
            reasoningTimeLimit,
            configFile
        ).getOrElse {
            send(error(it))
            return@channelFlow
        }.command

        val (vampireOutputBufferedReader, timeoutDeferred) = TheoremProverRunnerServiceImpl(
            command = command,
            timeLimit = reasoningTimeLimit,
            input = folFormula
        ).getOrElse { send(error(it)); close(); return@channelFlow }

        launch {
            QuestionAnsweringOutputToRdfSurfacesCascUseCase(
                qSurface = qSurface,
                questionAnsweringBufferedReader = vampireOutputBufferedReader,
            ).fold(
                onSuccess = {
                    if (it is AnswerTupleTransformationSuccess.NothingFound && timeoutDeferred.await()) {
                        send(success(TransformQaResult.Timeout))
                        close()
                        return@launch
                    }
                    send(success(it))
                },
                onFailure = { send(error(it)) }
            )
            cancel()
        }
    }
}

sealed interface TransformQaResult : Success {
    data object Timeout : TransformQaResult
}

sealed interface TransformQaError : RootError {
    data object NoQuestionSurface : TransformQaError
    data object MoreThanOneQuestionSurface : TransformQaError
}