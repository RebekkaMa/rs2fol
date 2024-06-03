package domain.use_cases

import domain.entities.rdf_term.IRI
import domain.error.*
import domain.use_cases.QaAnswerToRsError.*
import domain.use_cases.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.*

object CascQaAnswerToRsUseCase {

    operator fun invoke(
        inputStream: InputStream,
        rdfSurface: String,
        baseIri: IRI,
        outputPath: Path,
        rdfList: Boolean,
    ): Result<Success, RootError> {

        val qSurfaces =
            RDFSurfaceParseService(rdfList)
                .parseToEnd(rdfSurface, baseIri)
                .getOrElse { return Result.Error(it) }
                .getQSurfaces()


        if (qSurfaces.isEmpty()) return error(NoQuestionSurface)
        if (qSurfaces.size > 1) return error(MoreThanOneQuestionSurface)

        val qSurface = qSurfaces.single()

        val result = inputStream.bufferedReader().useLines { questionAnsweringOutputLines ->
            QuestionAnsweringOutputToRdfSurfacesCascUseCase.invoke(
                qSurface = qSurface,
                questionAnsweringOutputLines = questionAnsweringOutputLines
            )
        }

        if (outputPath.pathString == "-") return result

        FileService.createNewFile(
            path = outputPath,
            content = result.getOrElse { return Result.Error(it) }
        ).also {
            return success(QaAnswerToRsResult.WriteToFile(success = it))
        }
    }
}

sealed interface QaAnswerToRsResult : Success {
    data class WriteToFile(val success: Boolean) : QaAnswerToRsResult
}

sealed interface QaAnswerToRsError : RootError {
    data object NoQuestionSurface : QaAnswerToRsError
    data object MoreThanOneQuestionSurface : QaAnswerToRsError
}