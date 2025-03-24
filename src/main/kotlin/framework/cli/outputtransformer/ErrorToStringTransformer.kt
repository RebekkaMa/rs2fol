package framework.cli.outputtransformer

import app.interfaces.serviceResults.RdfSurfaceParserError
import app.interfaces.serviceResults.SZSParserServiceError
import app.interfaces.serviceResults.TheoremProverRunnerError
import app.interfaces.serviceResults.TptpTupleAnswerFormParserError
import app.use_cases.commands.QaAnswerToRsError
import app.use_cases.commands.RawQaAnswerToRsError
import app.use_cases.commands.TransformQaError
import app.use_cases.commands.subUseCase.AnswerTupleTransformationError
import app.use_cases.commands.subUseCase.GetTheoremProverCommandError
import app.use_cases.modelToString.LiteralTransformationError
import app.use_cases.modelTransformer.FOLGeneralTermToRDFSurfaceUseCaseError
import app.use_cases.modelTransformer.SurfaceNotSupportedError
import util.commandResult.RootError

object ErrorToStringTransformer {

    operator fun invoke(error: RootError): String {
        return TextStyler.error("Error: ") + when (error) {
            is SurfaceNotSupportedError -> {
                "Surface '${error.surface}' is not supported"
            }

            is LiteralTransformationError -> {
                "Literal '${error.literal}' can not be converted"
            }

            is app.use_cases.commands.CheckError -> {
                when (error) {
                    is app.use_cases.commands.CheckError.UnknownTheoremProverOutput -> "Vampire output is unknown and can not be interpreted"
                    is app.use_cases.commands.CheckError.VampireError -> "Vampire error"
                }
            }

            is TransformQaError -> {
                when (error) {
                    is TransformQaError.NoQuestionSurface -> "--q-surface - No question surface found. At least one is required."
                    is TransformQaError.MoreThanOneQuestionSurface -> "--q-surface - More than one question surface found. Only one is supported."
                }
            }

            is QaAnswerToRsError -> {
                when (error) {
                    is QaAnswerToRsError.NoQuestionSurface -> "No question surface found. At least one is required."
                    is QaAnswerToRsError.MoreThanOneQuestionSurface -> "More than one question surface found. Only one is supported."
                }
            }

            is RdfSurfaceParserError -> {
                when (error) {
                    is RdfSurfaceParserError.BlankNodeLabelCollision -> "Invalid blank node Label. Please rename all blank node labels that have the form 'BN_[0-9]+'."
                    is RdfSurfaceParserError.UndefinedPrefix -> "Undefined prefix: " + error.prefix
                    is RdfSurfaceParserError.LiteralNotValid -> "Not valid literal with value ${error.value} and iri ${error.iri}"
                    is RdfSurfaceParserError.GenericInvalidInput -> "Invalid Input. Please check the syntax!" + error.throwable.toString()
                }
            }

            is TptpTupleAnswerFormParserError -> {
                "Could not parse TPTP Tuple Answer '${error.tptpTuple}' - " +
                        when (error) {
                            is TptpTupleAnswerFormParserError.GenericInvalidInput -> "Invalid input. Please check the syntax! ${error.throwable}"
                        }
            }

            is FOLGeneralTermToRDFSurfaceUseCaseError -> {
                when (error) {
                    is FOLGeneralTermToRDFSurfaceUseCaseError.InvalidFunctionOrPredicate -> "Invalid function or predicate '${error.element}'"
                    is FOLGeneralTermToRDFSurfaceUseCaseError.InvalidElement -> "Invalid element '${error.element}'"
                }
            }

            is RawQaAnswerToRsError -> {
                when (error) {
                    RawQaAnswerToRsError.NoQuestionSurface -> "No question surface found. At least one is required."
                    RawQaAnswerToRsError.MoreThanOneQuestionSurface -> "More than one question surface found. Only one is supported."
                }
            }

            is app.use_cases.commands.subUseCase.TPTPTupleAnswerModelToN3SUseCaseError.InvalidInputError -> {
                "Invalid input affecting '${error.affectedFormula}', " + System.lineSeparator() + error.cause.toString()
            }

            is AnswerTupleTransformationError.AnswerTupleTransformation -> {
                "Error regarding ${error.affectedFormula}: " + invoke(
                    error.error
                )
            }

            is GetTheoremProverCommandError -> {
                when (error) {
                    is GetTheoremProverCommandError.TheoremProverNotFound -> "Theorem prover '${error.programName}' not found in config file"
                    is GetTheoremProverCommandError.TheoremProverOptionNotFound -> "Option '${error.programOption}' for program '${error.programName}' not found in config file"
                }
            }

            is SZSParserServiceError -> {
                "Could not parse SZS input: " +
                        when (error) {
                            SZSParserServiceError.OutputEndBeforeStart -> "SZS output end line occurred before a SZS output start line"
                            SZSParserServiceError.OutputStartBeforeEndAndStatus -> "SZS output start line occurred before previous output block was closed with SZS output end line"
                            SZSParserServiceError.OutputStartBeforeStatus -> "SZS output start occurred before a SZS status line"
                        }
            }

            is TheoremProverRunnerError -> {
                "Error while running theorem prover: " +
                        when (error) {
                            is TheoremProverRunnerError.CouldNotBeStarted -> "Theorem prover could not be started" + System.lineSeparator() + error.throwable.toString()
                            is TheoremProverRunnerError.CouldNotWriteInput -> "Could not write input to theorem prover" + System.lineSeparator() + error.throwable.toString()
                        }
            }

            else -> error.javaClass.simpleName

        }
    }
}