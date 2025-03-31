package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParseService
import app.use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import app.use_cases.modelTransformer.CanoncicalizeRDFSurfaceLiteralsUseCase
import app.use_cases.modelTransformer.FormulaRole
import app.use_cases.modelTransformer.RDFSurfaceModelToTPTPModelUseCase
import app.use_cases.results.TransformResult
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import util.commandResult.*
import java.nio.file.Path
import kotlin.io.path.pathString

class TransformUseCase(
    private val fileService: FileService,
    private val rdfSurfaceParseService: RDFSurfaceParseService,
    private val tPTPAnnotatedFormulaModelToStringUseCase : TPTPAnnotatedFormulaModelToStringUseCase,
    private val rdfSurfaceModelToTPTPModelUseCase: RDFSurfaceModelToTPTPModelUseCase,
    private val canoncicalizeRDFSurfaceLiteralsUseCase: CanoncicalizeRDFSurfaceLiteralsUseCase
) {

    operator fun invoke(
        rdfSurface: String,
        consequenceSurface: String?,
        ignoreQuerySurface: Boolean,
        useRdfLists: Boolean,
        baseIri: IRI,
        dEntailment: Boolean,
        outputPath: Path?,
        encode: Boolean,
    ): Flow<InfoResult<TransformResult.Success, RootError>> = flow {

        val parseResult = rdfSurfaceParseService.parseToEnd(rdfSurface, baseIri, useRdfLists)
        val axiomFormula = parseResult
            .runOnSuccess { successResult ->
                val surface = if (dEntailment) {
                    canoncicalizeRDFSurfaceLiteralsUseCase.invoke(successResult.positiveSurface).getOrElse {  err ->
                        emit(infoError(err))
                        return@flow
                    }
                } else successResult.positiveSurface
                rdfSurfaceModelToTPTPModelUseCase.invoke(
                    defaultPositiveSurface = surface,
                    ignoreQuerySurfaces = ignoreQuerySurface,
                )
            }
            .getOrElse {
                emit(infoError(it))
                return@flow
            }
            .joinToString(separator = System.lineSeparator()) { tPTPAnnotatedFormulaModelToStringUseCase.invoke(it, encode) }

        val consequenceFormula = consequenceSurface?.let {
            val consequenceParseResult = rdfSurfaceParseService.parseToEnd(it, baseIri, useRdfLists)
            consequenceParseResult
                .runOnSuccess { successResult ->
                    val surface = if (dEntailment) {
                        canoncicalizeRDFSurfaceLiteralsUseCase.invoke(successResult.positiveSurface).getOrElse {  err ->
                            emit(infoError(err))
                            return@flow
                        }
                    } else successResult.positiveSurface
                    rdfSurfaceModelToTPTPModelUseCase.invoke(
                        defaultPositiveSurface = surface,
                        ignoreQuerySurfaces = ignoreQuerySurface,
                        formulaRole = FormulaRole.Conjecture,
                    )
                }
                .getOrElse { err ->
                    emit(infoError(err))
                    return@flow
                }
                .joinToString(separator = System.lineSeparator()) { formula ->
                    tPTPAnnotatedFormulaModelToStringUseCase.invoke(formula, encode)
                }
        }

        val folFormula = axiomFormula + consequenceFormula?.let { System.lineSeparator() + it }.orEmpty()

        when {
            outputPath == null || outputPath.pathString == "-" -> {
                emit(infoSuccess(TransformResult.Success.WriteToLine(folFormula)))
            }

            else -> {
                fileService.createNewFile(
                    path = outputPath,
                    content = folFormula
                ).let { emit(infoSuccess(TransformResult.Success.WriteToFile(folFormula, it))) }
            }
        }
    }
}