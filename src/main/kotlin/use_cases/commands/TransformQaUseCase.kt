package use_cases.commands

import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import interface_adapters.services.vampire.TheoremProverService
import use_cases.GetTheoremProverCommandUseCase
import use_cases.commands.TransformQaError.MoreThanOneQuestionSurface
import use_cases.commands.TransformQaError.NoQuestionSurface
import use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import use_cases.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import util.error.*
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
        baseIri: IRI
    ): Result<Success, RootError> {

        val rdfSurface = inputStream.reader().use { it.readText() }
        val parseResult = RDFSurfaceParseService(useRdfLists).parseToEnd(rdfSurface, baseIri)
        val folFormula = parseResult
            .runOnSuccess { positiveSurface ->
                RdfSurfaceModelToTPTPModelUseCase(
                    defaultPositiveSurface = positiveSurface,
                    ignoreQuerySurfaces = false,
                )
            }
            .getOrElse { return error(it) }
            .joinToString(separator = System.lineSeparator()) { TPTPAnnotatedFormulaModelToStringUseCase(it) }

        val qSurfaces = parseResult.getOrElse { PositiveSurface() }.getQSurfaces()
        val qSurface = let {
            if (qSurfaces.isEmpty()) return error(NoQuestionSurface)
            if (qSurfaces.size > 1) return error(MoreThanOneQuestionSurface)
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
            reasoningTimeLimit
        ).getOrElse { return error(it) }.command

        val vampireParsingResult = TheoremProverService(
            command = command,
            timeLimit = reasoningTimeLimit,
            input = folFormula
        )?.useLines { vampireOutput ->
            QuestionAnsweringOutputToRdfSurfacesCascUseCase(
                qSurface = qSurface,
                questionAnsweringOutputLines = vampireOutput,
            )
        } ?: run { return success(TransformQaResult.Timeout) }

        return vampireParsingResult

    }
}

sealed interface TransformQaResult : Success {
    data object Timeout : TransformQaResult
}

sealed interface TransformQaError : RootError {
    data object NoQuestionSurface : TransformQaError
    data object MoreThanOneQuestionSurface : TransformQaError
}