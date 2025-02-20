package use_cases.commands

import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parser.RDFSurfaceParseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import use_cases.commands.QaAnswerToRsError.MoreThanOneQuestionSurface
import use_cases.commands.QaAnswerToRsError.NoQuestionSurface
import use_cases.commands.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import util.commandResult.*
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.pathString

object CascQaAnswerToRsUseCase {

    operator fun invoke(
        inputStream: InputStream,
        rdfSurface: String,
        baseIri: IRI,
        outputPath: Path,
        rdfList: Boolean,
    ): Flow<CommandStatus<Success, RootError>> = flow {

        val qSurfaces =
            RDFSurfaceParseService(rdfList)
                .parseToEnd(rdfSurface, baseIri)
                .getOrElse {
                    emit(CommandStatus.Error(it))
                    return@flow
                }
                .getQSurfaces()


        if (qSurfaces.isEmpty()) emit(error(NoQuestionSurface)).also { return@flow }
        if (qSurfaces.size > 1) emit(error(MoreThanOneQuestionSurface)).also { return@flow }

        val qSurface = qSurfaces.single()

        val result = QuestionAnsweringOutputToRdfSurfacesCascUseCase.invoke(
            qSurface = qSurface,
            questionAnsweringBufferedReader = inputStream.bufferedReader()
        )

        if (outputPath.pathString == "-") {
            result.fold(
                onSuccess = { emit(success(it)) },
                onFailure = { emit(error(it)) }
            )
            return@flow
        }

        FileService.createNewFile(
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