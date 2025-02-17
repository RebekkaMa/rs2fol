package use_cases.commands

import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import use_cases.modelToString.RdfSurfaceModelToN3UseCase
import util.commandResult.*
import java.nio.file.Path
import kotlin.io.path.pathString

object RewriteUseCase {

    operator fun invoke(
        rdfSurface: String,
        rdfList: Boolean,
        baseIRI: IRI,
        output: Path
    ): Flow<CommandStatus<RewriteResult, RootError>> = flow {

        val parserResult = RDFSurfaceParseService(rdfList).parseToEnd(rdfSurface, baseIRI)
        val result = parserResult.runOnSuccess { positiveSurface ->
            RdfSurfaceModelToN3UseCase(defaultPositiveSurface = positiveSurface)
        }.getOrElse {
            emit(error(it))
            return@flow
        }

        if (output.pathString == "-") emit(success(RewriteResult.WriteToLine(result))).also { return@flow }
        FileService.createNewFile(output, result).also { emit(success(RewriteResult.WriteToFile(it))) }
    }
}

sealed interface RewriteResult : Success {
    data class WriteToLine(val res: String) : RewriteResult
    data class WriteToFile(val success: Boolean) : RewriteResult
}