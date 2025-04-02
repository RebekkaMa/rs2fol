package adapter.presenter

import app.interfaces.results.RdfSurfaceParserResult
import app.interfaces.results.SZSParserServiceResult
import app.interfaces.results.TheoremProverRunnerResult
import app.interfaces.results.TptpTupleAnswerFormParserResult
import app.interfaces.services.presenter.ErrorToStringTransformerService
import app.interfaces.services.presenter.TextStylerService
import app.use_cases.results.CascQaAnswerToRsResult
import app.use_cases.results.CheckResult
import app.use_cases.results.RawQaAnswerToRsResult
import app.use_cases.results.TransformQaResult
import app.use_cases.results.modelToString.RdfSurfaceModelToN3sResult
import app.use_cases.results.modelTransformerResults.FOLGeneralTermToRDFSurfaceResult
import app.use_cases.results.modelTransformerResults.RDFSurfaceModelToFOLModelResult
import app.use_cases.results.subUseCaseResults.GetTheoremProverCommandResult
import app.use_cases.results.subUseCaseResults.QuestionAnsweringOutputToRdfSurfacesCascResult
import app.use_cases.results.subUseCaseResults.TPTPTupleAnswerModelToN3SResult
import entities.rdfsurfaces.results.RdfSurfaceResult
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

            is RdfSurfaceModelToN3sResult.Error -> {
                when (error) {
                    is RdfSurfaceModelToN3sResult.Error.LiteralTransformationError -> "Literal (value='${error.value},iri=${error.iri})' can not be converted to N3S"
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

            is CascQaAnswerToRsResult.Error -> {
                when (error) {
                    is CascQaAnswerToRsResult.Error.NoQuestionSurface -> "No question surface found. At least one is required."
                    is CascQaAnswerToRsResult.Error.MoreThanOneQuestionSurface -> "More than one question surface found. Only one is supported."
                }
            }

            is RdfSurfaceParserResult.Error -> {
                when (error) {
                    is RdfSurfaceParserResult.Error.BlankNodeLabelCollision -> "Invalid blank node Label. Please rename all blank node labels that have the form 'BN_[0-9]+'."
                    is RdfSurfaceParserResult.Error.UndefinedPrefix -> "Undefined prefix: " + error.prefix
                    is RdfSurfaceParserResult.Error.LiteralNotValid -> "Not valid literal with value ${error.value} and iri ${error.iri}"
                    is RdfSurfaceParserResult.Error.GenericInvalidInput -> {
                        buildString {
                            append("Invalid Input. Please check the syntax!")
                            if (debug) return@buildString
                            append(System.lineSeparator())
                            append(textStylerService.bold("Cause: "))
                            append(error.cause)
                        }
                    }

                    is RdfSurfaceParserResult.Error.SurfaceNotSupported -> "Surface '${error.surface}' is not supported"

                }
            }

            is TptpTupleAnswerFormParserResult.Error.GenericInvalidInput -> {
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

            is RawQaAnswerToRsResult.Error -> {
                when (error) {
                    RawQaAnswerToRsResult.Error.NoQuestionSurface -> "No question surface found. At least one is required."
                    RawQaAnswerToRsResult.Error.MoreThanOneQuestionSurface -> "More than one question surface found. Only one is supported."
                }
            }

            is TPTPTupleAnswerModelToN3SResult.Error.TransformationError -> {
                "Invalid input affecting '${error.affectedFormula}': " + System.lineSeparator() + invoke(
                    error.error,
                    debug = debug
                )
            }

            is QuestionAnsweringOutputToRdfSurfacesCascResult.Error.AnswerTupleTransformation -> {
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

            is RdfSurfaceResult.Error -> {
                when (error) {
                    RdfSurfaceResult.Error.TupleArityUnequalToGraffitiCount -> "The arity of the answer tuples doesn't match the number of graffiti on the negative answer surface!"
                }
            }

            else -> error.javaClass.simpleName
        }
    }
}