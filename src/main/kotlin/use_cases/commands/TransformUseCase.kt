package use_cases.commands

import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parser.RDFSurfaceParseServiceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import use_cases.commands.TransformUseCaseSuccess.WriteToFile
import use_cases.commands.TransformUseCaseSuccess.WriteToLine
import use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import use_cases.modelTransformer.FormulaRole
import use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import util.commandResult.*
import java.nio.file.Path
import kotlin.io.path.pathString

object TransformUseCase {

    operator fun invoke(
        rdfSurface: String,
        consequenceSurface: String?,
        ignoreQuerySurface: Boolean,
        useRdfLists: Boolean,
        baseIri: IRI,
        dEntailment: Boolean,
        outputPath: Path?
    ): Flow<CommandStatus<TransformUseCaseSuccess, RootError>> = flow {

        val parseResult = RDFSurfaceParseServiceImpl(useRdfLists).parseToEnd(rdfSurface, baseIri)
        val axiomFormula = parseResult
            .runOnSuccess { positiveSurface ->
                RdfSurfaceModelToTPTPModelUseCase(
                    defaultPositiveSurface = positiveSurface,
                    ignoreQuerySurfaces = ignoreQuerySurface,
                    dEntailment = dEntailment
                )
            }
            .getOrElse {
                emit(error(it))
                return@flow
            }
            .joinToString(separator = System.lineSeparator()) { TPTPAnnotatedFormulaModelToStringUseCase(it) }

        val consequenceFormula = consequenceSurface?.let {
            val consequenceParseResult = RDFSurfaceParseServiceImpl(useRdfLists).parseToEnd(it, baseIri)
            consequenceParseResult
                .runOnSuccess { positiveSurface ->
                    RdfSurfaceModelToTPTPModelUseCase(
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
                    TPTPAnnotatedFormulaModelToStringUseCase(formula)
                }
        }

        val folFormula = axiomFormula + consequenceFormula?.let { System.lineSeparator() + it }.orEmpty()

        when {
            outputPath == null || outputPath.pathString == "-" -> {
                emit(success(WriteToLine(folFormula)))
            }

            else -> {
                FileService.createNewFile(
                    path = outputPath,
                    content = folFormula
                ).let { emit(success(WriteToFile(folFormula, it))) }
            }
        }
    }
}

sealed interface TransformUseCaseSuccess : Success {
    val res: String

    data class WriteToLine(override val res: String) : TransformUseCaseSuccess
    data class WriteToFile(override val res: String, val success: Boolean) : TransformUseCaseSuccess
}