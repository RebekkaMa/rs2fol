package use_cases.subUseCase

import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import entities.rdfsurfaces.QSurface
import interface_adapters.services.parsing.TptpTupleAnswerFormToModelService
import util.error.*

object QuestionAnsweringOutputToRdfSurfacesCascUseCase {

    operator fun invoke(
        qSurface: QSurface,
        questionAnsweringOutputLines: Sequence<String>,
    ): Result<Success, RootError> {
        val parsedResult = mutableSetOf<TPTPTupleAnswerFormAnswer>()
        var refutationFound = false

        for (line in questionAnsweringOutputLines) {
            if (line.contains("SZS answers Tuple")) {
                val tptpAnswerTupleList = line.substringAfter('[').substringBefore(']')

                TptpTupleAnswerFormToModelService.parseToEnd(tptpAnswerTupleList).fold(
                    onSuccess = {
                        parsedResult.add(it)
                    },
                    onFailure = {
                        return Result.Error(
                            AnswerTupleTransformationError.AnswerTupleTransformation(
                                affectedFormula = tptpAnswerTupleList,
                                error = it
                            )
                        )
                    }
                )
            }
            if (line.contains("(SZS status ContradictoryAxioms for)|(^\\s*unsat\\s*$)".toRegex(RegexOption.IGNORE_CASE))) refutationFound =
                true
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
            return Result.Success(
                AnswerTupleTransformationSuccess.Refutation
            )
        }

        return if (parsedResult.isEmpty()) {
            Result.Success(AnswerTupleTransformationSuccess.NothingFound)
        } else {
            TPTPTupleAnswerModelToRdfSurfaceUseCase(
                tptpTupleAnswerFormAnswer = allAnswers,
                qSurface = qSurface
            ).map { AnswerTupleTransformationSuccess.Success(it) }
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