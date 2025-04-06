package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParserService
import app.interfaces.services.TheoremProverRunnerService
import app.use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
import app.use_cases.commands.subUseCase.QuestionAnsweringOutputToRDFSurfacesCascUseCase
import app.use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import app.use_cases.modelTransformer.CanonicalizeRDFSurfaceLiteralsUseCase
import app.use_cases.modelTransformer.RDFSurfaceModelToTPTPAnnotatedFormulaUseCase
import app.use_cases.results.commands.TransformQaResult
import app.use_cases.results.subUseCaseResults.QuestionAnsweringOutputToRDFSurfacesCascResult
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import util.commandResult.*
import java.io.InputStream
import java.nio.file.Path

class TransformQaUseCase(
    private val rdfSurfaceParserService: RDFSurfaceParserService,
    private val theoremProverRunnerService: TheoremProverRunnerService,
    private val fileService: FileService,
    private val tPTPAnnotatedFormulaModelToStringUseCase: TPTPAnnotatedFormulaModelToStringUseCase,
    private val rdfSurfaceModelToTPTPAnnotatedFormulaUseCase: RDFSurfaceModelToTPTPAnnotatedFormulaUseCase,
    private val getTheoremProverCommandUseCase: GetTheoremProverCommandUseCase,
    private val questionAnsweringOutputToRdfSurfacesCascUseCase: QuestionAnsweringOutputToRDFSurfacesCascUseCase,
    private val canonicalizeRDFSurfaceLiteralsUseCase: CanonicalizeRDFSurfaceLiteralsUseCase,
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
        encode: Boolean
        ): Flow<InfoResult<Success, RootError>> = channelFlow {

        val rdfSurface = inputStream.reader().use { it.readText() }
        val parseResult = rdfSurfaceParserService.parseToEnd(rdfSurface, baseIri, useRdfLists)
        val folFormula = parseResult
            .runOnSuccess { successResult ->
                val surface = if (dEntailment) {
                    canonicalizeRDFSurfaceLiteralsUseCase.invoke(successResult.positiveSurface).getOrElse { err ->
                        send(infoError(err))
                        return@channelFlow
                    }
                } else successResult.positiveSurface
                rdfSurfaceModelToTPTPAnnotatedFormulaUseCase.invoke(
                    defaultPositiveSurface = surface,
                    ignoreQuerySurfaces = false,
                )
            }
            .getOrElse {
                send(infoError(it))
                return@channelFlow
            }
            .joinToString(separator = System.lineSeparator()) { tPTPAnnotatedFormulaModelToStringUseCase.invoke(it, encode = encode) }

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
                    if (it is QuestionAnsweringOutputToRDFSurfacesCascResult.Success.NothingFound && timeoutDeferred.await()) {
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