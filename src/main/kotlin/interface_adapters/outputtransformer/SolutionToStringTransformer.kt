package interface_adapters.outputtransformer

import domain.error.Success
import domain.use_cases.*
import domain.use_cases.subUseCase.AnswerTupleTransformationSuccess

class SolutionToStringTransformer {

    fun transform(success: Success): String? {

        return when (success) {
            is CheckSuccess -> {
                when (success) {
                    CheckSuccess.Consequence -> "Consequence"
                    CheckSuccess.NoConsequence -> "No consequence"
                    CheckSuccess.Timeout -> "Timeout"
                }
            }

            is TransformQaResult -> {
                when (success) {
                    is TransformQaResult.Timeout -> "Timeout"
                }
            }

            is QaAnswerToRsResult -> {
                when (success) {
                    is QaAnswerToRsResult.WriteToFile -> ""
                }
            }

            is RawQaAnswerToRsResult -> {
                when (success) {
                    is RawQaAnswerToRsResult.WriteToLine -> success.res
                    is RawQaAnswerToRsResult.WriteToFile -> ""
                }
            }

            is RewriteResult -> {
                when (success) {
                    is RewriteResult.WriteToLine -> success.res
                    is RewriteResult.WriteToFile -> ""
                }
            }

            is TransformUseCaseSuccess -> when (success) {
                is TransformUseCaseSuccess.WriteToLine -> success.res
                is TransformUseCaseSuccess.WriteToFile -> ""
            }

            is AnswerTupleTransformationSuccess -> when (success) {
                is AnswerTupleTransformationSuccess.Refutation -> "Refutation found"
                is AnswerTupleTransformationSuccess.NothingFound -> "No answer found"
                is AnswerTupleTransformationSuccess.Success -> success.answer
            }
            else -> null
        }
    }
}