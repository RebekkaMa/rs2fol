package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParseService
import app.use_cases.commands.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import util.commandResult.*
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.pathString

class CascQaAnswerToRsUseCase(
    private val rdfSurfaceParseService: RDFSurfaceParseService,
    private val fileService: FileService,
    private val questionAnsweringOutputToRdfSurfacesCascUseCase: QuestionAnsweringOutputToRdfSurfacesCascUseCase
) {

    operator fun invoke(
        inputStream: InputStream,
        rdfSurface: String,
        baseIri: IRI,
        outputPath: Path,
        rdfList: Boolean,
    ): Flow<CommandStatus<Success, RootError>> = flow {

        val qSurfaces =
            rdfSurfaceParseService
                .parseToEnd(rdfSurface, baseIri, rdfList)
                .getOrElse {
                    emit(CommandStatus.Error(it))
                    return@flow
                }
                .getQSurfaces()


        if (qSurfaces.isEmpty()) emit(error(QaAnswerToRsError.NoQuestionSurface)).also { return@flow }
        if (qSurfaces.size > 1) emit(error(QaAnswerToRsError.MoreThanOneQuestionSurface)).also { return@flow }

        val qSurface = qSurfaces.single()

        val result = questionAnsweringOutputToRdfSurfacesCascUseCase.invoke(
            qSurface = qSurface,
            questionAnsweringBufferedReader = inputStream.bufferedReader(),
        )

        if (outputPath.pathString == "-") {
            result.fold(
                onSuccess = { emit(success(it)) },
                onFailure = { emit(error(it)) }
            )
            return@flow
        }

        fileService.createNewFile(
            path = outputPath,
            content = result.getOrElse {
                emit(CommandStatus.Error(it))
                return@flow
            }
        ).also {
            emit(success(QaAnswerToRsResult.WriteToFile(success = it)))
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