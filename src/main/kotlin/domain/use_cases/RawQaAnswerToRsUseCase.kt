package domain.use_cases

import domain.entities.rdf_term.IRI
import domain.error.*
import domain.use_cases.RawQaAnswerToRsError.MoreThanOneQuestionSurface
import domain.use_cases.RawQaAnswerToRsError.NoQuestionSurface
import domain.use_cases.subUseCase.ParsedQuestionAnsweringResultToRdfSurfaceUseCase
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import interface_adapters.services.parsing.TptpTupleAnswerFormParserService
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.*

object RawQaAnswerToRsUseCase {
    operator fun invoke(
        inputStream: InputStream,
        rdfSurface: String,
        baseIri: IRI,
        outputPath: Path,
        rdfList: Boolean,
    ): Result<Success, RootError> {

        val qSurfaces = RDFSurfaceParseService(rdfList)
            .parseToEnd(rdfSurface, baseIri)
            .getOrElse { return error(it) }
            .getQSurfaces()

        if (qSurfaces.isEmpty()) return error(NoQuestionSurface)
        if (qSurfaces.size > 1) return error(MoreThanOneQuestionSurface)

        val qSurface = qSurfaces.single()

        val tptpTupleAnswer = inputStream.bufferedReader().use { it.readText() }
        val result = TptpTupleAnswerFormParserService.parseToEnd(tptpTupleAnswer).runOnSuccess {
                ParsedQuestionAnsweringResultToRdfSurfaceUseCase(
                    resultList = it.first.toSet(),
                    qSurface = qSurface
                )
            }.getOrElse { return error(it) }

        if (outputPath.pathString == "-") {
            return success(RawQaAnswerToRsResult.WriteToLine(result))
        }

        FileService.createNewFile(
            path = outputPath,
            content = result
        ).also {
            return success(RawQaAnswerToRsResult.WriteToFile(success = it))
        }
    }
}

sealed interface RawQaAnswerToRsResult :Success {
    data class WriteToLine(val res: String) : RawQaAnswerToRsResult
    data class WriteToFile(val success: Boolean) : RawQaAnswerToRsResult
}

sealed interface RawQaAnswerToRsError : RootError {
    data object NoQuestionSurface : RawQaAnswerToRsError
    data object MoreThanOneQuestionSurface : RawQaAnswerToRsError
}