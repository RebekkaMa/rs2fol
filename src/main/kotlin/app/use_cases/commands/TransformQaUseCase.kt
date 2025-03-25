package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParseService
import app.interfaces.services.TheoremProverRunnerService
import app.use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
import app.use_cases.commands.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import app.use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import app.use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import app.use_cases.results.TransformQaResult
import app.use_cases.results.subUseCaseResults.QuestionAnsweringOutputToRdfSurfacesCascResult
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
        ): Flow<InfoResult<Success, RootError>> = channelFlow {

        val rdfSurface = inputStream.reader().use { it.readText() }
        val parseResult = rdfSurfaceParseService.parseToEnd(rdfSurface, baseIri, useRdfLists)
        val folFormula = parseResult
            .runOnSuccess { successResult ->
                rdfSurfaceModelToTPTPModelUseCase.invoke(
                    defaultPositiveSurface = successResult.positiveSurface,
                    ignoreQuerySurfaces = false,
                    dEntailment = dEntailment
                )
            }
            .getOrElse {
                send(infoError(it))
                return@channelFlow
            }
            .joinToString(separator = System.lineSeparator()) { tPTPAnnotatedFormulaModelToStringUseCase.invoke(it) }

        val qSurfaces = parseResult.getSuccessOrNull()?.positiveSurface?.getQSurfaces() ?: emptyList()
        val qSurface = when {
            qSurfaces.isEmpty() -> {
                send(infoError(TransformQaResult.Error.NoQuestionSurface))
                return@channelFlow
            }

            qSurfaces.size > 1 -> {
                send(infoError(TransformQaResult.Error.MoreThanOneQuestionSurface))
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
            send(infoError(it))
            return@channelFlow
        }.command

        val (vampireOutputBufferedReader, timeoutDeferred) = theoremProverRunnerService(
            command = command,
            timeLimit = reasoningTimeLimit,
            input = folFormula
        ).getOrElse { send(infoError(it)); close(); return@channelFlow }.output

        launch {
            questionAnsweringOutputToRdfSurfacesCascUseCase.invoke(
                qSurface = qSurface,
                questionAnsweringBufferedReader = vampireOutputBufferedReader,
            ).fold(
                onSuccess = {
                    if (it is QuestionAnsweringOutputToRdfSurfacesCascResult.Success.NothingFound && timeoutDeferred.await()) {
                        send(infoSuccess(TransformQaResult.Success.Timeout))
                        close()
                        return@launch
                    }
                    send(infoSuccess(it))
                },
                onFailure = { send(infoError(it)) }
            )
            cancel()
        }
    }
}