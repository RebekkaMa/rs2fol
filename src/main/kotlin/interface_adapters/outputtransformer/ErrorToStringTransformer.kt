package interface_adapters.outputtransformer

import domain.error.RootError
import domain.use_cases.CheckError
import domain.use_cases.QaAnswerToRsError
import domain.use_cases.RawQaAnswerToRsError
import domain.use_cases.TransformQaError
import domain.use_cases.subUseCase.AnswerTupleTransformationError
import domain.use_cases.subUseCase.InvalidInputError
import domain.use_cases.transform.LiteralTransformationError
import domain.use_cases.transform.SurfaceNotSupportedError
import interface_adapters.services.parsing.RdfSurfaceParserError
import interface_adapters.services.parsing.TptpTupleAnswerFormParserError

class ErrorToStringTransformer {

    fun transform(error: RootError): String? {
       return when (error) {
           is SurfaceNotSupportedError -> "Surface '${error.surface}' is not supported"
           is LiteralTransformationError -> "Literal '${error.literal}' can not be converted"
           is CheckError -> when (error) {
               is CheckError.UnknownVampireOutput -> "Vampire output is unknown and can not be interpreted"
               is CheckError.VampireError -> "Vampire error"
           }
           is TransformQaError -> when (error) {
               is TransformQaError.NoQuestionSurface -> "--q-surface - No question surface found. At least one is required."
               is TransformQaError.MoreThanOneQuestionSurface -> "--q-surface - More than one question surface found. Only one is supported."
           }
           is QaAnswerToRsError -> when (error) {
               is QaAnswerToRsError.NoQuestionSurface -> "No question surface found. At least one is required."
               is QaAnswerToRsError.MoreThanOneQuestionSurface -> "More than one question surface found. Only one is supported."
           }
           is RdfSurfaceParserError -> when (error) {
               is RdfSurfaceParserError.BlankNodeLabelCollision -> "Invalid blank node Label. Please rename all blank node labels that have the form 'BN_[0-9]+'."
               is RdfSurfaceParserError.UndefinedPrefix -> "Undefined prefix: " + error.prefix
               is RdfSurfaceParserError.LiteralNotValid -> "Not valid literal with value ${error.value} and iri ${error.iri}"
               is RdfSurfaceParserError.GenericInvalidInput -> "Invalid Input. Please check the syntax!" + error.throwable.toString()
           }
           is TptpTupleAnswerFormParserError -> when (error) {
               is TptpTupleAnswerFormParserError.InvalidFunctionOrPredicate -> "Invalid function or predicate '${error.element}'"
               is TptpTupleAnswerFormParserError.InvalidElement -> "Invalid element '${error.element}'"
               is TptpTupleAnswerFormParserError.GenericInvalidInput -> "Invalid input. Please check the syntax!" + error.throwable.toString()
           }
           is RawQaAnswerToRsError -> when (error) {
               RawQaAnswerToRsError.NoQuestionSurface -> "No question surface found. At least one is required."
               RawQaAnswerToRsError.MoreThanOneQuestionSurface -> "More than one question surface found. Only one is supported."
           }
           is InvalidInputError -> "Invalid input affecting '${error.affectedFormula}', " + System.lineSeparator() + error.cause.toString()
           is AnswerTupleTransformationError.AnswerTupleTransformation -> "Error regarding ${error.affectedFormula}: " + transform(error.error)
           else -> null
           }
    }
}