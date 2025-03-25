package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParseService
import app.interfaces.services.TptpTupleAnswerFormParserService
import app.use_cases.commands.subUseCase.TPTPTupleAnswerModelToRdfSurfaceUseCase
import app.use_cases.results.RawQaAnswerToRsResult
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
    ): Flow<InfoResult<RawQaAnswerToRsResult.Success, RootError>> = flow {

        val qSurfaces = rdfSurfaceParseService
            .parseToEnd(rdfSurface, baseIri, rdfList)
            .getOrElse {
                emit(infoError(it))
                return@flow
            }
            .positiveSurface
            .getQSurfaces()

        if (qSurfaces.isEmpty()) emit(infoError(RawQaAnswerToRsResult.Error.NoQuestionSurface))
        if (qSurfaces.size > 1) emit(infoError(RawQaAnswerToRsResult.Error.MoreThanOneQuestionSurface))

        val qSurface = qSurfaces.single()

        val tptpTupleAnswer = inputStream.bufferedReader().use { it.readText() }

        val result = tptpTupleAnswerFormParserService.parseToEnd(tptpTupleAnswer).runOnSuccess {
            tPTPTupleAnswerModelToRdfSurfaceUseCase.invoke(
                answerTuples = it.tPTPTupleAnswerFormAnswer.answerTuples,
                qSurface = qSurface
            )
        }.getOrElse {
            emit(infoError(it))
            return@flow
        }

        if (outputPath.pathString == "-") {
            emit(infoSuccess(RawQaAnswerToRsResult.Success.WriteToLine(result)))
            return@flow
        }

        fileService.createNewFile(
            path = outputPath,
            content = result
        ).also {
            emit(infoSuccess(RawQaAnswerToRsResult.Success.WriteToFile(success = it)))
        }
    }
}

