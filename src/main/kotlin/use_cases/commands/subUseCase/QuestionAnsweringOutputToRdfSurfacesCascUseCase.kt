package use_cases.commands.subUseCase

import entities.SZSAnswerTupleFormModel
import entities.SZSOutputModel
import entities.SZSStatus
import entities.SZSStatusType
import entities.fol.tptp.AnswerTuple
import entities.rdfsurfaces.QSurface
import interface_adapters.services.parser.SZSParserServiceImpl
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transformWhile
import util.commandResult.*
import java.io.BufferedReader

object QuestionAnsweringOutputToRdfSurfacesCascUseCase {

    suspend operator fun invoke(
        qSurface: QSurface,
        questionAnsweringBufferedReader: BufferedReader,
    ): IntermediateStatus<AnswerTupleTransformationSuccess, RootError> {
        val answerTuples = mutableSetOf<AnswerTuple>()
        var refutationFound = false

        val error = SZSParserServiceImpl()
            .parse(questionAnsweringBufferedReader)
            .transformWhile { res ->
                res.fold(
                    onSuccess = { szsModel ->
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

        if (error != null) return IntermediateStatus.Error(error)

        if (refutationFound) {
            if (qSurface.graffiti.isEmpty()) {
                return TPTPTupleAnswerModelToRdfSurfaceUseCase(
                    answerTuples = answerTuples.toList(),
                    qSurface = qSurface
                ).map { AnswerTupleTransformationSuccess.Success(it) }
            }
            return IntermediateStatus.Result(
                AnswerTupleTransformationSuccess.Refutation
            )
        }


        return if (answerTuples.isEmpty()) {
            IntermediateStatus.Result(AnswerTupleTransformationSuccess.NothingFound)
        } else {
            TPTPTupleAnswerModelToRdfSurfaceUseCase(
                answerTuples = answerTuples.toList(),
                qSurface = qSurface
            ).map {
                AnswerTupleTransformationSuccess.Success(it)
            }
        }
    }
}

sealed interface AnswerTupleTransformationError : Error {
    data class AnswerTupleTransformation(val affectedFormula: String? = null, val error: Error) :
        AnswerTupleTransformationError
}

sealed interface AnswerTupleTransformationSuccess : Success {
    data object Refutation : AnswerTupleTransformationSuccess
    data object NothingFound : AnswerTupleTransformationSuccess
    data class Success(val answer: String) : AnswerTupleTransformationSuccess
}