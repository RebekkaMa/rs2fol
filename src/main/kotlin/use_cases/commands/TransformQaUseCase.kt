package use_cases.commands

import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parser.RDFSurfaceParseService
import interface_adapters.services.theoremProver.TheoremProverRunnerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import use_cases.commands.TransformQaError.MoreThanOneQuestionSurface
import use_cases.commands.TransformQaError.NoQuestionSurface
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
    ): Flow<CommandStatus<Success, RootError>> = flow {

        val rdfSurface = inputStream.reader().use { it.readText() }
        val parseResult = RDFSurfaceParseService(useRdfLists).parseToEnd(rdfSurface, baseIri)
        val folFormula = parseResult
            .runOnSuccess { positiveSurface ->
                RdfSurfaceModelToTPTPModelUseCase(
                    defaultPositiveSurface = positiveSurface,
                    ignoreQuerySurfaces = false,
                    dEntailment = dEntailment
                )
            }
            .getOrElse {
                emit(error(it))
                return@flow
            }
            .joinToString(separator = System.lineSeparator()) { TPTPAnnotatedFormulaModelToStringUseCase(it) }

        val qSurfaces = parseResult.getOrElse { PositiveSurface() }.getQSurfaces()
        val qSurface = let {
            if (qSurfaces.isEmpty()) emit(error(NoQuestionSurface)).also { return@flow }
            if (qSurfaces.size > 1) emit(error(MoreThanOneQuestionSurface)).also { return@flow }
            qSurfaces.single()
        }

        outputPath?.let {
            FileService.createNewFile(
                path = outputPath,
                content = folFormula
            )
        }

        val command = GetTheoremProverCommandUseCase(
            programName,
            optionId,
            reasoningTimeLimit,
            configFile
        ).getOrElse {
            emit(error(it))
            return@flow
        }.command

        val vampireOutputBufferedReader = TheoremProverRunnerService(
            command = command,
            timeLimit = reasoningTimeLimit,
            input = folFormula
        ) ?: run {
            emit(success<Success, RootError>(TransformQaResult.Timeout))
            return@flow
        }

        QuestionAnsweringOutputToRdfSurfacesCascUseCase(
            qSurface = qSurface,
            questionAnsweringBufferedReader = vampireOutputBufferedReader,
        ).fold(
            onSuccess = { emit(CommandStatus.Result<Success, RootError>(it)) },
            onFailure = { emit(CommandStatus.Error<Success, RootError>(it)) }
        )

    }
}

sealed interface TransformQaResult : Success {
    data object Timeout : TransformQaResult
}

sealed interface TransformQaError : RootError {
    data object NoQuestionSurface : TransformQaError
    data object MoreThanOneQuestionSurface : TransformQaError
}