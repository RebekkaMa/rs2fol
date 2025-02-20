package interface_adapters.outputtransformer

import use_cases.commands.*
import use_cases.commands.subUseCase.AnswerTupleTransformationSuccess
import util.commandResult.Success

object SolutionToStringTransformer {

    operator fun invoke(success: Success): String? {

        return when (success) {
            is CheckSuccess -> {
                when (success) {
                    CheckSuccess.Consequence -> TextStyler.boldGreen("Consequence")
                    CheckSuccess.NoConsequence -> TextStyler.boldRed("No consequence")
                    CheckSuccess.Timeout -> TextStyler.info("Timeout")
                    is CheckSuccess.NotKnown -> TextStyler.info("Not known: ${success.szsStatusType}")
                }
            }

            is TransformQaResult -> {
                when (success) {
                    is TransformQaResult.Timeout -> TextStyler.info("Timeout")
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