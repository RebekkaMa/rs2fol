package domain.use_cases.subUseCase

import domain.entities.QSurface
import domain.entities.rdf_term.RdfTerm
import domain.error.*
import interface_adapters.services.parsing.TptpTupleAnswerFormParserService

object QuestionAnsweringOutputToRdfSurfacesCascUseCase {

    operator fun invoke(
        qSurface: QSurface,
        questionAnsweringOutputLines: Sequence<String>,
    ): Result<Success, RootError> {
        val parsedResult = mutableSetOf<List<RdfTerm>>()

        var refutationFound = false

        for (line in questionAnsweringOutputLines) {
            if (line.contains("SZS answers Tuple")) {
                val tptpAnswerTupleList =
                    line
                        .trimStart { char -> char != '[' }
                        .trimEnd { char -> char != ']' }

                TptpTupleAnswerFormParserService.parseToEnd(tptpAnswerTupleList).fold(
                    onSuccess = {
                        parsedResult.addAll(it.first)
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

        if (refutationFound) {
            if (qSurface.graffiti.isEmpty()) {
                return ParsedQuestionAnsweringResultToRdfSurfaceUseCase(
                    resultList = setOf(listOf()),
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
            ParsedQuestionAnsweringResultToRdfSurfaceUseCase(
                resultList = parsedResult,
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