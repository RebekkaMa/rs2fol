package adapter.presenter

import app.interfaces.results.RDFSurfaceParserResult
import app.interfaces.results.SZSParserServiceResult
import app.interfaces.results.TPTPTupleAnswerFormParserResult
import app.interfaces.results.TheoremProverRunnerResult
import app.interfaces.services.presenter.ErrorToStringTransformerService
import app.interfaces.services.presenter.TextStylerService
import app.use_cases.results.commands.CascQaAnswerToRSResult
import app.use_cases.results.commands.CheckResult
import app.use_cases.results.commands.RawQaAnswerToRSResult
import app.use_cases.results.commands.TransformQaResult
import app.use_cases.results.modelToString.RDFSurfaceModelToN3SResult
import app.use_cases.results.modelTransformerResults.FOLGeneralTermToRDFSurfaceResult
import app.use_cases.results.modelTransformerResults.RDFSurfaceModelToFOLModelResult
import app.use_cases.results.subUseCaseResults.GetTheoremProverCommandResult
import app.use_cases.results.subUseCaseResults.QuestionAnsweringOutputToRDFSurfacesCascResult
import app.use_cases.results.subUseCaseResults.TPTPTupleAnswerModelToN3SResult
import entities.rdfsurfaces.results.RDFSurfaceResult
import util.commandResult.RootError

class ErrorToStringTransformerServiceImpl(private val textStylerService: TextStylerService) :
    ErrorToStringTransformerService {

    override operator fun invoke(error: RootError, debug: Boolean): String {
        return textStylerService.error("Error: ") + when (error) {
            is RDFSurfaceModelToFOLModelResult.Error -> {
                when (error) {
                    is RDFSurfaceModelToFOLModelResult.Error.SurfaceNotSupported -> "Surface '${error.surface}' is not supported"
                }
            }

            is RDFSurfaceModelToN3SResult.Error -> {
                when (error) {
                    is RDFSurfaceModelToN3SResult.Error.LiteralTransformationError -> "Literal (value='${error.value},iri=${error.iri})' can not be converted to N3S"
                }

            }

            is CheckResult.Error -> {
                when (error) {
                    is CheckResult.Error.UnknownTheoremProverOutput -> "Vampire output is unknown and can not be interpreted"
                    is CheckResult.Error.VampireError -> "Vampire error"
                }
            }

            is TransformQaResult.Error -> {
                when (error) {
                    is TransformQaResult.Error.NoQuestionSurface -> "--q-surface - No negative answer surface found. At least one is required."
                    is TransformQaResult.Error.MoreThanOneQuestionSurface -> "--q-surface - More than one negative answer surface found. Only one is supported."
                }
            }

            is CascQaAnswerToRSResult.Error -> {
                when (error) {
                    is CascQaAnswerToRSResult.Error.NoQuestionSurface -> "No question surface found. At least one is required."
                    is CascQaAnswerToRSResult.Error.MoreThanOneQuestionSurface -> "More than one question surface found. Only one is supported."
                }
            }

            is RDFSurfaceParserResult.Error -> {
                when (error) {
                    is RDFSurfaceParserResult.Error.BlankNodeLabelCollision -> "Invalid blank node Label. Please rename all blank node labels that have the form 'BN_[0-9]+'."
                    is RDFSurfaceParserResult.Error.UndefinedPrefix -> "Undefined prefix: " + error.prefix
                    is RDFSurfaceParserResult.Error.LiteralNotValid -> "Not valid literal with value ${error.value} and iri ${error.iri}"
                    is RDFSurfaceParserResult.Error.GenericInvalidInput -> {
                        buildString {
                            append("Invalid Input. Please check the syntax!")
                            if (debug) return@buildString
                            append(System.lineSeparator())
                            append(textStylerService.bold("Cause: "))
                            append(error.cause)
                        }
                    }

                    is RDFSurfaceParserResult.Error.SurfaceNotSupported -> "Surface '${error.surface}' is not supported"

                }
            }

            is TPTPTupleAnswerFormParserResult.Error.GenericInvalidInput -> {
                buildString {
                    append("Could not parse TPTP Tuple Answer '${error.tptpTuple}'")
                    append("Invalid input. Please check the syntax!")
                    if (debug) return@buildString
                    append(System.lineSeparator())
                    append(textStylerService.bold("Cause: "))
                    append(error.throwable.toString())
                }
            }

            is FOLGeneralTermToRDFSurfaceResult.Error -> {
                when (error) {
                    is FOLGeneralTermToRDFSurfaceResult.Error.InvalidFunctionOrPredicate -> "Invalid function or predicate '${error.element}'"
                    is FOLGeneralTermToRDFSurfaceResult.Error.InvalidElement -> "Invalid element '${error.element}'"
                }
            }

            is RawQaAnswerToRSResult.Error -> {
                when (error) {
                    RawQaAnswerToRSResult.Error.NoQuestionSurface -> "No question surface found. At least one is required."
                    RawQaAnswerToRSResult.Error.MoreThanOneQuestionSurface -> "More than one question surface found. Only one is supported."
                }
            }

            is TPTPTupleAnswerModelToN3SResult.Error.TransformationError -> {
                "Invalid input affecting '${error.affectedFormula}': " + System.lineSeparator() + invoke(
                    error.error,
                    debug = debug
                )
            }

            is QuestionAnsweringOutputToRDFSurfacesCascResult.Error.AnswerTupleTransformation -> {
                "Error regarding ${error.affectedFormula}: " + invoke(error.error, debug = debug)
            }

            is GetTheoremProverCommandResult.Error -> {
                when (error) {
                    is GetTheoremProverCommandResult.Error.TheoremProverNotFound -> "Theorem prover '${error.programName}' not found in config file"
                    is GetTheoremProverCommandResult.Error.TheoremProverOptionNotFound -> "Option '${error.programOption}' for program '${error.programName}' not found in config file"
                }
            }

            is SZSParserServiceResult.Error -> {
                "Could not parse SZS input: " +
                        when (error) {
                            SZSParserServiceResult.Error.OutputEndBeforeStart -> "SZS output end line occurred before a SZS output start line"
                            SZSParserServiceResult.Error.OutputStartBeforeEndAndStatus -> "SZS output start line occurred before previous output block was closed with SZS output end line"
                            SZSParserServiceResult.Error.OutputStartBeforeStatus -> "SZS output start occurred before a SZS status line"
                        }
            }

            is TheoremProverRunnerResult.Error -> {
                "Error while running theorem prover: " +
                        when (error) {
                            is TheoremProverRunnerResult.Error.CouldNotBeStarted -> {
                                buildString {
                                    append("Theorem prover could not be started")
                                    if (debug) return@buildString
                                    append(System.lineSeparator())
                                    append(textStylerService.bold("Cause: "))
                                    append("${error.throwable}")
                                }
                            }
                            is TheoremProverRunnerResult.Error.CouldNotWriteInput -> {
                                buildString {
                                    append("Could not write input to theorem prover")
                                    if (debug) return@buildString
                                    append(System.lineSeparator())
                                    append(textStylerService.bold("Cause: "))
                                    append("${error.throwable}")
                                }
                            }
                        }
            }

            is RDFSurfaceResult.Error -> {
                when (error) {
                    RDFSurfaceResult.Error.TupleArityUnequalToGraffitiCount -> "The arity of the answer tuples doesn't match the number of graffiti on the negative answer surface!"
                }
            }

            else -> error.javaClass.simpleName
        }
    }
}