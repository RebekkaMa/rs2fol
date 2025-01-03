package domain.use_cases

import domain.entities.PositiveSurface
import domain.entities.rdf_term.IRI
import domain.error.*
import domain.use_cases.TransformQaError.MoreThanOneQuestionSurface
import domain.use_cases.TransformQaError.NoQuestionSurface
import domain.use_cases.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import domain.use_cases.transform.RdfSurfaceModelToFolUseCase
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import interface_adapters.services.vampire.VampireService
import java.io.InputStream
import java.nio.file.Path

class TransformQaUseCase  {

    operator fun invoke(
        inputStream: InputStream,
        vampireMode: Int,
        vampireExecutable: Path,
        vampireTimeLimit: Long,
        outputPath: Path?,
        useRdfLists: Boolean,
        baseIri: IRI
    ) : Result<Success, RootError> {

        val rdfSurface = inputStream.reader().use { it.readText() }
        val parseResult = RDFSurfaceParseService(useRdfLists).parseToEnd(rdfSurface, baseIri)
        val folFormula = parseResult.runOnSuccess { positiveSurface ->
            RdfSurfaceModelToFolUseCase(
                defaultPositiveSurface = positiveSurface,
                ignoreQuerySurfaces = false,
            )
        }.getOrElse { return error(it) }

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

        val vampireParsingResult = VampireService.startForQuestionAnswering(
            vampireOption = vampireMode,
            vampireExec = vampireExecutable,
            timeLimit = vampireTimeLimit,
            input = folFormula
        )?.useLines { vampireOutput ->
            QuestionAnsweringOutputToRdfSurfacesCascUseCase(
                qSurface = qSurface,
                questionAnsweringOutputLines = vampireOutput,
            )
        } ?: run { return success(TransformQaResult.Timeout)  }

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