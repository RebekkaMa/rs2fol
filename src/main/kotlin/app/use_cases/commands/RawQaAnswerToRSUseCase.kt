package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParserService
import app.interfaces.services.TPTPTupleAnswerFormParserService
import app.use_cases.commands.subUseCase.TPTPTupleAnswerModelToN3SUseCase
import app.use_cases.results.commands.RawQaAnswerToRSResult
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import util.commandResult.*
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.pathString

class RawQaAnswerToRSUseCase(
    private val rdfSurfaceParserService: RDFSurfaceParserService,
    private val tptpTupleAnswerFormParserService: TPTPTupleAnswerFormParserService,
    private val fileService: FileService,
    private val tPTPTupleAnswerModelToN3SUseCase: TPTPTupleAnswerModelToN3SUseCase
) {
    operator fun invoke(
        inputStream: InputStream,
        rdfSurface: String,
        baseIri: IRI,
        outputPath: Path,
        rdfList: Boolean,
    ): Flow<InfoResult<RawQaAnswerToRSResult.Success, RootError>> = flow {

        val qSurfaces = rdfSurfaceParserService
            .parseToEnd(rdfSurface, baseIri, rdfList)
            .getOrElse {
                emit(infoError(it))
                return@flow
            }
            .positiveSurface
            .getQSurfaces()

        if (qSurfaces.isEmpty()) emit(infoError(RawQaAnswerToRSResult.Error.NoQuestionSurface))
        if (qSurfaces.size > 1) emit(infoError(RawQaAnswerToRSResult.Error.MoreThanOneQuestionSurface))

        val qSurface = qSurfaces.single()

        val tptpTupleAnswer = inputStream.bufferedReader().use { it.readText() }

        val result = tptpTupleAnswerFormParserService.parseToEnd(tptpTupleAnswer).runOnSuccess {
            tPTPTupleAnswerModelToN3SUseCase.invoke(
                answerTuples = it.tPTPTupleAnswerFormAnswer.answerTuples,
                qSurface = qSurface
            )
        }.getOrElse {
            emit(infoError(it))
            return@flow
        }

        if (outputPath.pathString == "-") {
            emit(infoSuccess(RawQaAnswerToRSResult.Success.WriteToLine(result)))
            return@flow
        }

        fileService.createNewFile(
            path = outputPath,
            content = result
        ).also {
            emit(infoSuccess(RawQaAnswerToRSResult.Success.WriteToFile(success = it)))
        }
    }
}

