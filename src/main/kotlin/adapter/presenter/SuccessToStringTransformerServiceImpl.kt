package adapter.presenter

import app.interfaces.services.presenter.SuccessToStringTransformerService
import app.interfaces.services.presenter.TextStylerService
import app.use_cases.results.commands.*
import app.use_cases.results.subUseCaseResults.QuestionAnsweringOutputToRDFSurfacesCascResult
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

            is CascQaAnswerToRSResult.Success -> {
                when (success) {
                    is CascQaAnswerToRSResult.Success.WriteToLine -> success.res
                    is CascQaAnswerToRSResult.Success.WriteToFile -> ""
                }
            }

            is RawQaAnswerToRSResult.Success -> {
                when (success) {
                    is RawQaAnswerToRSResult.Success.WriteToLine -> success.res
                    is RawQaAnswerToRSResult.Success.WriteToFile -> ""
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

            is QuestionAnsweringOutputToRDFSurfacesCascResult.Success -> when (success) {
                is QuestionAnsweringOutputToRDFSurfacesCascResult.Success.Refutation -> "Refutation found"
                is QuestionAnsweringOutputToRDFSurfacesCascResult.Success.NothingFound -> "No answer found"
                is QuestionAnsweringOutputToRDFSurfacesCascResult.Success.Answer -> success.data
            }

            else -> null
        }
    }
}