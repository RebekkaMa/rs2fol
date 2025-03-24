package use_cases.commands

import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parser.RDFSurfaceParseServiceImpl
import interface_adapters.services.parser.TptpTupleAnswerFormToModelServiceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import use_cases.commands.RawQaAnswerToRsError.MoreThanOneQuestionSurface
import use_cases.commands.RawQaAnswerToRsError.NoQuestionSurface
import use_cases.commands.subUseCase.TPTPTupleAnswerModelToRdfSurfaceUseCase
import util.commandResult.*
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.pathString

object RawQaAnswerToRsUseCase {
    operator fun invoke(
        inputStream: InputStream,
        rdfSurface: String,
        baseIri: IRI,
        outputPath: Path,
        rdfList: Boolean,
    ): Flow<CommandStatus<Success, RootError>> = flow {

        val qSurfaces = RDFSurfaceParseServiceImpl(rdfList)
            .parseToEnd(rdfSurface, baseIri)
            .getOrElse {
                emit(error(it))
                return@flow
            }
            .getQSurfaces()

        if (qSurfaces.isEmpty()) emit(error(NoQuestionSurface))
        if (qSurfaces.size > 1) emit(error(MoreThanOneQuestionSurface))

        val qSurface = qSurfaces.single()

        val tptpTupleAnswer = inputStream.bufferedReader().use { it.readText() }

        val result = TptpTupleAnswerFormToModelServiceImpl.parseToEnd(tptpTupleAnswer).runOnSuccess {
            TPTPTupleAnswerModelToRdfSurfaceUseCase(
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

        FileService.createNewFile(
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