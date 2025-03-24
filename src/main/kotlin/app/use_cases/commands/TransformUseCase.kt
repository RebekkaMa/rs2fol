package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParseService
import app.use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import app.use_cases.modelTransformer.FormulaRole
import app.use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
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
    private val rdfSurfaceModelToTPTPModelUseCase: RdfSurfaceModelToTPTPModelUseCase
) {

    operator fun invoke(
        rdfSurface: String,
        consequenceSurface: String?,
        ignoreQuerySurface: Boolean,
        useRdfLists: Boolean,
        baseIri: IRI,
        dEntailment: Boolean,
        outputPath: Path?,
    ): Flow<CommandStatus<TransformUseCaseSuccess, RootError>> = flow {

        val parseResult = rdfSurfaceParseService.parseToEnd(rdfSurface, baseIri, useRdfLists)
        val axiomFormula = parseResult
            .runOnSuccess { positiveSurface ->
                rdfSurfaceModelToTPTPModelUseCase.invoke(
                    defaultPositiveSurface = positiveSurface,
                    ignoreQuerySurfaces = ignoreQuerySurface,
                    dEntailment = dEntailment
                )
            }
            .getOrElse {
                emit(error(it))
                return@flow
            }
            .joinToString(separator = System.lineSeparator()) { tPTPAnnotatedFormulaModelToStringUseCase.invoke(it) }

        val consequenceFormula = consequenceSurface?.let {
            val consequenceParseResult = rdfSurfaceParseService.parseToEnd(it, baseIri, useRdfLists)
            consequenceParseResult
                .runOnSuccess { positiveSurface ->
                    rdfSurfaceModelToTPTPModelUseCase.invoke(
                        defaultPositiveSurface = positiveSurface,
                        ignoreQuerySurfaces = ignoreQuerySurface,
                        formulaRole = FormulaRole.Conjecture,
                        dEntailment = dEntailment
                    )
                }
                .getOrElse { err ->
                    emit(error(err))
                    return@flow
                }
                .joinToString(separator = System.lineSeparator()) { formula ->
                    tPTPAnnotatedFormulaModelToStringUseCase.invoke(formula)
                }
        }

        val folFormula = axiomFormula + consequenceFormula?.let { System.lineSeparator() + it }.orEmpty()

        when {
            outputPath == null || outputPath.pathString == "-" -> {
                emit(success(TransformUseCaseSuccess.WriteToLine(folFormula)))
            }

            else -> {
                fileService.createNewFile(
                    path = outputPath,
                    content = folFormula
                ).let { emit(success(TransformUseCaseSuccess.WriteToFile(folFormula, it))) }
            }
        }
    }
}

sealed interface TransformUseCaseSuccess : Success {
    val res: String

    data class WriteToLine(override val res: String) : TransformUseCaseSuccess
    data class WriteToFile(override val res: String, val success: Boolean) : TransformUseCaseSuccess
}