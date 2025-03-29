package adapter.presenter

import app.interfaces.services.presenter.SuccessToStringTransformerService
import app.interfaces.services.presenter.TextStylerService
import app.use_cases.results.*
import app.use_cases.results.subUseCaseResults.QuestionAnsweringOutputToRdfSurfacesCascResult
import util.commandResult.Success

class SuccessToStringTransformerServiceImpl(private val textStyler: TextStylerService) : SuccessToStringTransformerService {

    override operator fun invoke(success: Success): String? {

        return when (success) {
            is CheckResult.Success -> {
                when (success) {
                    CheckResult.Success.Consequence -> textStyler.boldGreen("Consequence")
                    CheckResult.Success.NoConsequence -> textStyler.boldRed("No consequence")
                    CheckResult.Success.Timeout -> textStyler.info("Timeout")
                    CheckResult.Success.Contradiction -> textStyler.bold("Contradiction")
                    is CheckResult.Success.NotKnown -> textStyler.info("Not known: ${success.szsStatusType}")
                    CheckResult.Success.Satisfiable -> textStyler.boldGreen("Satisfiable")
                    CheckResult.Success.Unsatisfiable -> textStyler.boldRed("Unsatisfiable")
                }
            }

            is TransformQaResult.Success -> {
                when (success) {
                    is TransformQaResult.Success.Timeout -> textStyler.info("Timeout")
                }
            }

            is CascQaAnswerToRsResult.Success -> {
                when (success) {
                    is CascQaAnswerToRsResult.Success.WriteToLine -> success.res
                    is CascQaAnswerToRsResult.Success.WriteToFile -> ""
                }
            }

            is RawQaAnswerToRsResult.Success -> {
                when (success) {
                    is RawQaAnswerToRsResult.Success.WriteToLine -> success.res
                    is RawQaAnswerToRsResult.Success.WriteToFile -> ""
                }
            }

            is RewriteResult.Success -> {
                when (success) {
                    is RewriteResult.Success.WriteToLine -> success.res
                    is RewriteResult.Success.WriteToFile -> ""
                }
            }

            is TransformResult.Success -> when (success) {
                is TransformResult.Success.WriteToLine -> success.res
                is TransformResult.Success.WriteToFile -> ""
            }

            is QuestionAnsweringOutputToRdfSurfacesCascResult.Success -> when (success) {
                is QuestionAnsweringOutputToRdfSurfacesCascResult.Success.Refutation -> "Refutation found"
                is QuestionAnsweringOutputToRdfSurfacesCascResult.Success.NothingFound -> "No answer found"
                is QuestionAnsweringOutputToRdfSurfacesCascResult.Success.Answer -> success.data
            }

            else -> null
        }
    }
}