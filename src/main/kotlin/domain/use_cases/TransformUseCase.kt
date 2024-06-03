package domain.use_cases

import domain.entities.rdf_term.IRI
import domain.error.*
import domain.use_cases.TransformUseCaseSuccess.*
import domain.use_cases.transform.RdfSurfaceModelToFolUseCase
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import java.nio.file.Path
import kotlin.io.path.*

object TransformUseCase {

    operator fun invoke(
        rdfSurface: String,
        outputPath: Path,
        ignoreQuerySurface: Boolean,
        useRdfLists: Boolean,
        baseIri: IRI
    ): Result<TransformUseCaseSuccess, RootError> {

        val parseResult = RDFSurfaceParseService(useRdfLists).parseToEnd(rdfSurface, baseIri)
        val folFormula = parseResult.runOnSuccess { positiveSurface ->
            RdfSurfaceModelToFolUseCase(
                defaultPositiveSurface = positiveSurface,
                ignoreQuerySurfaces = ignoreQuerySurface,
            )
        }.getOrElse { return error(it) }

        return if (outputPath.pathString == "-") {
            success(WriteToLine(folFormula))
        } else {
            FileService.createNewFile(
                path = outputPath,
                content = folFormula
            ).let { success(WriteToFile(it)) }
        }
    }
}

sealed interface TransformUseCaseSuccess : Success {
    data class WriteToLine(val res: String) : TransformUseCaseSuccess
    data class WriteToFile(val success: Boolean) : TransformUseCaseSuccess
}