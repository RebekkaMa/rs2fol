package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParseService
import app.interfaces.services.TptpTupleAnswerFormParserService
import app.use_cases.commands.subUseCase.TPTPTupleAnswerModelToRdfSurfaceUseCase
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import util.commandResult.*
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.pathString

class RawQaAnswerToRsUseCase(
    private val rdfSurfaceParseService: RDFSurfaceParseService,
    private val tptpTupleAnswerFormParserService: TptpTupleAnswerFormParserService,
    private val fileService: FileService,
    private val tPTPTupleAnswerModelToRdfSurfaceUseCase: TPTPTupleAnswerModelToRdfSurfaceUseCase
) {
    operator fun invoke(
        inputStream: InputStream,
        rdfSurface: String,
        baseIri: IRI,
        outputPath: Path,
        rdfList: Boolean,
    ): Flow<CommandStatus<Success, RootError>> = flow {

        val qSurfaces = rdfSurfaceParseService
            .parseToEnd(rdfSurface, baseIri, rdfList)
            .getOrElse {
                emit(error(it))
                return@flow
            }
            .getQSurfaces()

        if (qSurfaces.isEmpty()) emit(error(RawQaAnswerToRsError.NoQuestionSurface))
        if (qSurfaces.size > 1) emit(error(RawQaAnswerToRsError.MoreThanOneQuestionSurface))

        val qSurface = qSurfaces.single()

        val tptpTupleAnswer = inputStream.bufferedReader().use { it.readText() }

        val result = tptpTupleAnswerFormParserService.parseToEnd(tptpTupleAnswer).runOnSuccess {
            tPTPTupleAnswerModelToRdfSurfaceUseCase.invoke(
                answerTuples = it.answerTuples,
                qSurface = qSurface
            )
        }.getOrElse {
            emit(error(it))
            return@flow
        }

        if (outputPath.pathString == "-") {
            emit(success(RawQaAnswerToRsResult.WriteToLine(result)))
            return@flow
        }

        fileService.createNewFile(
            path = outputPath,
            content = result
        ).also {
            emit(success(RawQaAnswerToRsResult.WriteToFile(success = it)))
        }
    }
}

sealed interface RawQaAnswerToRsResult : Success {
    data class WriteToLine(val res: String) : RawQaAnswerToRsResult
    data class WriteToFile(val success: Boolean) : RawQaAnswerToRsResult
}

sealed interface RawQaAnswerToRsError : RootError {
    data object NoQuestionSurface : RawQaAnswerToRsError
    data object MoreThanOneQuestionSurface : RawQaAnswerToRsError
}