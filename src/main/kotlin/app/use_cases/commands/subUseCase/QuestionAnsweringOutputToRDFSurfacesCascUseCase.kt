package app.use_cases.commands.subUseCase

import app.interfaces.services.SZSParserService
import app.use_cases.results.subUseCaseResults.QuestionAnsweringOutputToRDFSurfacesCascResult
import entities.SZSAnswerTupleFormModel
import entities.SZSOutputModel
import entities.SZSStatus
import entities.SZSStatusType
import entities.fol.tptp.AnswerTuple
import entities.rdfsurfaces.QSurface
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transformWhile
import util.commandResult.Result
import util.commandResult.RootError
import util.commandResult.fold
import util.commandResult.map
import java.io.BufferedReader

class QuestionAnsweringOutputToRDFSurfacesCascUseCase(
    private val tptpTupleAnswerModelToN3SUseCase: TPTPTupleAnswerModelToN3SUseCase,
    private val szsParserService: SZSParserService
) {

    suspend operator fun invoke(
        qSurface: QSurface,
        questionAnsweringBufferedReader: BufferedReader,
    ): Result<QuestionAnsweringOutputToRDFSurfacesCascResult.Success, RootError> {
        val answerTuples = mutableSetOf<AnswerTuple>()
        var refutationFound = false

        val error = szsParserService
            .parse(questionAnsweringBufferedReader)
            .transformWhile { res ->
                res.fold(
                    onSuccess = { successResult ->
                        val szsModel = successResult.szsModel
                        when (szsModel) {
                            is SZSAnswerTupleFormModel -> {
                                answerTuples.addAll(szsModel.tptpTupleAnswerFormAnswer.answerTuples)
                                true
                            }

                            is SZSStatus, is SZSOutputModel -> {
                                if (szsModel.statusType == SZSStatusType.SuccessOntology.CONTRADICTORY_AXIOMS) {
                                    refutationFound = true
                                    false
                                } else true

                            }

                            else -> {
                                true
                            }
                        }
                    },
                    onFailure = {
                        emit(it)
                        false
                    }
                )
            }.firstOrNull()

        if (error != null) return Result.Error(error)

        if (refutationFound) {
            if (qSurface.graffiti.isEmpty()) {
                return tptpTupleAnswerModelToN3SUseCase(
                    answerTuples = answerTuples.toList(),
                    qSurface = qSurface
                ).map { QuestionAnsweringOutputToRDFSurfacesCascResult.Success.Answer(it) }
            }
            return Result.Success(
                QuestionAnsweringOutputToRDFSurfacesCascResult.Success.Refutation
            )
        }

        return if (answerTuples.isEmpty()) {
            Result.Success(QuestionAnsweringOutputToRDFSurfacesCascResult.Success.NothingFound)
        } else {
            tptpTupleAnswerModelToN3SUseCase(
                answerTuples = answerTuples.toList(),
                qSurface = qSurface
            ).map {
                QuestionAnsweringOutputToRDFSurfacesCascResult.Success.Answer(it)
            }
        }
    }
}
