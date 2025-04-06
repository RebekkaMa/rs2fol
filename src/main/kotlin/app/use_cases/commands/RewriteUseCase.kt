package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParserService
import app.use_cases.modelToString.RdfSurfaceModelToN3UseCase
import app.use_cases.modelTransformer.CanonicalizeRDFSurfaceLiteralsUseCase
import app.use_cases.results.commands.RewriteResult
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import util.commandResult.*
import java.nio.file.Path
import kotlin.io.path.pathString

class RewriteUseCase(
    private val fileService: FileService,
    private val rdfSurfaceParserService: RDFSurfaceParserService,
    private val rdfSurfaceModelToN3UseCase : RdfSurfaceModelToN3UseCase,
    private val canonicalizeRDFSurfaceLiteralsUseCase: CanonicalizeRDFSurfaceLiteralsUseCase
) {

    operator fun invoke(
        rdfSurface: String,
        rdfList: Boolean,
        baseIRI: IRI,
        output: Path,
        dEntailment: Boolean,
        encode: Boolean
    ): Flow<InfoResult<RewriteResult.Success, RootError>> = flow {

        val parserResult = rdfSurfaceParserService.parseToEnd(rdfSurface, baseIRI, rdfList)
        val result = parserResult.runOnSuccess { successResult ->
            val surface = if (dEntailment) {
                canonicalizeRDFSurfaceLiteralsUseCase.invoke(successResult.positiveSurface).getOrElse { err ->
                    emit(infoError(err))
                    return@flow
                }
            } else successResult.positiveSurface
            rdfSurfaceModelToN3UseCase.invoke(
                defaultPositiveSurface = surface,
                encode = encode
            )
        }.getOrElse {
            emit(infoError(it))
            return@flow
        }

        if (output.pathString == "-") emit(infoSuccess(RewriteResult.Success.WriteToLine(result))).also { return@flow }
        fileService.createNewFile(output, result).also { emit(infoSuccess(RewriteResult.Success.WriteToFile(it))) }
    }
}