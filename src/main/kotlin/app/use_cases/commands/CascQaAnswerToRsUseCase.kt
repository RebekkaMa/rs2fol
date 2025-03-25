package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.RDFSurfaceParseService
import app.interfaces.services.presenter.SuccessToStringTransformerService
import app.use_cases.commands.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import app.use_cases.results.CascQaAnswerToRsResult
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
    private val successToStringTransformerService: SuccessToStringTransformerService,
    private val questionAnsweringOutputToRdfSurfacesCascUseCase: QuestionAnsweringOutputToRdfSurfacesCascUseCase
) {

    operator fun invoke(
        inputStream: InputStream,
        rdfSurface: String,
        baseIri: IRI,
        outputPath: Path,
        rdfList: Boolean,
    ): Flow<InfoResult<Success, RootError>> = flow {

        val qSurfaces = rdfSurfaceParseService
            .parseToEnd(rdfSurface, baseIri, rdfList)
            .getOrElse {
                emit(InfoResult.Error(it))
                return@flow
            }.positiveSurface.getQSurfaces()


        if (qSurfaces.isEmpty()) emit(infoError(CascQaAnswerToRsResult.Error.NoQuestionSurface)).also { return@flow }
        if (qSurfaces.size > 1) emit(infoError(CascQaAnswerToRsResult.Error.MoreThanOneQuestionSurface)).also { return@flow }

        val qSurface = qSurfaces.single()

        val result = questionAnsweringOutputToRdfSurfacesCascUseCase.invoke(
            qSurface = qSurface,
            questionAnsweringBufferedReader = inputStream.bufferedReader(),
        )

        if (outputPath.pathString == "-") {
            result.fold(
                onSuccess = { emit(infoSuccess(it)) },
                onFailure = { emit(infoError(it)) }
            )
            return@flow
        }

        fileService.createNewFile(
            path = outputPath,
            content = result.getOrElse {
                emit(infoError(it))
                return@flow
            }.let { successToStringTransformerService(it).orEmpty() }
        ).also {
            emit(infoSuccess(CascQaAnswerToRsResult.Success.WriteToFile(success = it)))
        }
    }
}