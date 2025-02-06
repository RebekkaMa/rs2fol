package use_cases.commands

import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import use_cases.commands.TransformUseCaseSuccess.WriteToFile
import use_cases.commands.TransformUseCaseSuccess.WriteToLine
import use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import util.error.*
import java.nio.file.Path
import kotlin.io.path.pathString

object TransformUseCase {

    operator fun invoke(
        rdfSurface: String,
        outputPath: Path,
        ignoreQuerySurface: Boolean,
        useRdfLists: Boolean,
        baseIri: IRI
    ): Result<TransformUseCaseSuccess, RootError> {

        val parseResult = RDFSurfaceParseService(useRdfLists).parseToEnd(rdfSurface, baseIri)
        val folFormula = parseResult
            .runOnSuccess { positiveSurface ->
                RdfSurfaceModelToTPTPModelUseCase(
                    defaultPositiveSurface = positiveSurface,
                    ignoreQuerySurfaces = ignoreQuerySurface,
                )
            }
            .getOrElse { return error(it) }
            .joinToString(separator = System.lineSeparator()) { TPTPAnnotatedFormulaModelToStringUseCase(it) }

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