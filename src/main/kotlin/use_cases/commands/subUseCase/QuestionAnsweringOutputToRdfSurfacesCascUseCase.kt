package use_cases.commands.subUseCase

import entities.SZSAnswerTupleFormModel
import entities.SZSOutputModel
import entities.SZSStatus
import entities.SZSStatusType
import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import entities.rdfsurfaces.QSurface
import interface_adapters.services.parser.SZSParserService
import util.commandResult.*
import java.io.BufferedReader

object QuestionAnsweringOutputToRdfSurfacesCascUseCase {

    operator fun invoke(
        qSurface: QSurface,
        questionAnsweringBufferedReader: BufferedReader,
    ): IntermediateStatus<Success, RootError> {
        val parsedResult = mutableSetOf<TPTPTupleAnswerFormAnswer>()
        var refutationFound = false

        val questionAnsweringTPTPAnswers = SZSParserService().parse(questionAnsweringBufferedReader)
        /*
                    return IntermediateStatus.Error(
                        AnswerTupleTransformationError.AnswerTupleTransformation(
                            affectedFormula = tptpAnswerTupleList,
                            error = it
                        )
                    )
                */


        questionAnsweringTPTPAnswers.forEach {
            when (it) {
                is SZSAnswerTupleFormModel -> {
                    parsedResult.add(it.tptpTupleAnswerFormAnswer)
                }

                is SZSStatus, is SZSOutputModel -> {
                    if (it.statusType == SZSStatusType.SuccessOntology.CONTRADICTORY_AXIOMS) refutationFound = true
                }

            }
        }

        val allAnswers = parsedResult.fold(TPTPTupleAnswerFormAnswer()) { acc, tptpTupleAnswerFormAnswer ->
            acc.copy(
                answerTuples = acc.answerTuples + tptpTupleAnswerFormAnswer.answerTuples,
                disjunctiveAnswerTuples = acc.disjunctiveAnswerTuples + tptpTupleAnswerFormAnswer.disjunctiveAnswerTuples,
            )
        }

        if (refutationFound) {
            if (qSurface.graffiti.isEmpty()) {
                return TPTPTupleAnswerModelToRdfSurfaceUseCase(
                    tptpTupleAnswerFormAnswer = TPTPTupleAnswerFormAnswer(),
                    qSurface = qSurface
                ).map { AnswerTupleTransformationSuccess.Success(it) }
            }
            return IntermediateStatus.Result(
                AnswerTupleTransformationSuccess.Refutation
            )
        }

        return if (parsedResult.isEmpty()) {
            IntermediateStatus.Result(AnswerTupleTransformationSuccess.NothingFound)
        } else {
            TPTPTupleAnswerModelToRdfSurfaceUseCase(
                tptpTupleAnswerFormAnswer = allAnswers,
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