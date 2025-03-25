package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParseService
import app.use_cases.modelToString.RdfSurfaceModelToN3UseCase
import app.use_cases.results.RewriteResult
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import util.commandResult.*
import java.nio.file.Path
import kotlin.io.path.pathString

class RewriteUseCase(
    private val fileService: FileService,
    private val rdfSurfaceParseService: RDFSurfaceParseService,
    private val rdfSurfaceModelToN3UseCase : RdfSurfaceModelToN3UseCase
) {

    operator fun invoke(
        rdfSurface: String,
        rdfList: Boolean,
        baseIRI: IRI,
        output: Path,
        dEntailment: Boolean,
    ): Flow<InfoResult<RewriteResult.Success, RootError>> = flow {

        val parserResult = rdfSurfaceParseService.parseToEnd(rdfSurface, baseIRI, rdfList)
        val result = parserResult.runOnSuccess { successResult ->
            rdfSurfaceModelToN3UseCase.invoke(
                defaultPositiveSurface = successResult.positiveSurface,
                dEntailment = dEntailment
            )
        }.getOrElse {
            emit(infoError(it))
            return@flow
        }

        if (output.pathString == "-") emit(infoSuccess(RewriteResult.Success.WriteToLine(result))).also { return@flow }
        fileService.createNewFile(output, result).also { emit(infoSuccess(RewriteResult.Success.WriteToFile(it))) }
    }
}