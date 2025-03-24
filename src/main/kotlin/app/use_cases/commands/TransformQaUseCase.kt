package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParseService
import app.interfaces.services.TheoremProverRunnerService
import app.use_cases.commands.subUseCase.AnswerTupleTransformationSuccess
import app.use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
import app.use_cases.commands.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import app.use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import app.use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import util.commandResult.*
import java.io.InputStream
import java.nio.file.Path

class TransformQaUseCase(
    private val rdfSurfaceParseService: RDFSurfaceParseService,
    private val theoremProverRunnerService: TheoremProverRunnerService,
    private val fileService: FileService,
    private val tPTPAnnotatedFormulaModelToStringUseCase: TPTPAnnotatedFormulaModelToStringUseCase,
    private val rdfSurfaceModelToTPTPModelUseCase: RdfSurfaceModelToTPTPModelUseCase,
    private val getTheoremProverCommandUseCase: GetTheoremProverCommandUseCase,
    private val questionAnsweringOutputToRdfSurfacesCascUseCase: QuestionAnsweringOutputToRdfSurfacesCascUseCase
) {

    operator fun invoke(
        inputStream: InputStream,
        optionId: Int,
        programName: String,
        reasoningTimeLimit: Long,
        outputPath: Path?,
        useRdfLists: Boolean,
        baseIri: IRI,
        configFile: Path,
        dEntailment: Boolean,
        ): Flow<CommandStatus<Success, RootError>> = channelFlow {

        val rdfSurface = inputStream.reader().use { it.readText() }
        val parseResult = rdfSurfaceParseService.parseToEnd(rdfSurface, baseIri, useRdfLists)
        val folFormula = parseResult
            .runOnSuccess { positiveSurface ->
                rdfSurfaceModelToTPTPModelUseCase.invoke(
                    defaultPositiveSurface = positiveSurface,
                    ignoreQuerySurfaces = false,
                    dEntailment = dEntailment
                )
            }
            .getOrElse {
                send(error(it))
                return@channelFlow
            }
            .joinToString(separator = System.lineSeparator()) { tPTPAnnotatedFormulaModelToStringUseCase.invoke(it) }

        val qSurfaces = parseResult.getOrElse { PositiveSurface() }.getQSurfaces()
        val qSurface = when {
            qSurfaces.isEmpty() -> {
                send(error(TransformQaError.NoQuestionSurface))
                return@channelFlow
            }

            qSurfaces.size > 1 -> {
                send(error(TransformQaError.MoreThanOneQuestionSurface))
                return@channelFlow
            }

            else -> qSurfaces.single()
        }

        outputPath?.let {
            fileService.createNewFile(
                path = it,
                content = folFormula
            )
        }

        val command = getTheoremProverCommandUseCase.invoke(
            programName,
            optionId,
            reasoningTimeLimit,
            configFile,
        ).getOrElse {
            send(error(it))
            return@channelFlow
        }.command

        val (vampireOutputBufferedReader, timeoutDeferred) = theoremProverRunnerService(
            command = command,
            timeLimit = reasoningTimeLimit,
            input = folFormula
        ).getOrElse { send(error(it)); close(); return@channelFlow }

        launch {
            questionAnsweringOutputToRdfSurfacesCascUseCase.invoke(
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