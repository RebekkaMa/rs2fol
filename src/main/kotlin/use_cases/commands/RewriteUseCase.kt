package use_cases.commands

import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import use_cases.modelToString.RdfSurfaceModelToN3UseCase
import util.error.*
import java.nio.file.Path
import kotlin.io.path.pathString

object RewriteUseCase {

    operator fun invoke(
        rdfSurface: String,
        rdfList: Boolean,
        baseIRI: IRI,
        output: Path
    ): Result<RewriteResult, RootError> {

        val parserResult = RDFSurfaceParseService(rdfList).parseToEnd(rdfSurface, baseIRI)
        val result = parserResult.runOnSuccess { positiveSurface ->
            RdfSurfaceModelToN3UseCase(defaultPositiveSurface = positiveSurface)
        }.getOrElse { return error(it) }

        if (output.pathString == "-") return success(RewriteResult.WriteToLine(result))
        FileService.createNewFile(output, result).also { return success(RewriteResult.WriteToFile(it)) }
    }
}

sealed interface RewriteResult : Success {
    data class WriteToLine(val res: String) : RewriteResult
    data class WriteToFile(val success: Boolean) : RewriteResult
}