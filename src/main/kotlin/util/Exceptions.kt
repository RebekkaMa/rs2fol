package util

import com.github.h0tk3y.betterParse.parser.ErrorResult

const val generalParseErrorString = "Failed to parse RDF surface. Please check the syntax."

class TransformerException(message: String, cause: Throwable? = null) : Exception(message, cause)
class LiteralTransformationException(val literal: String) : Exception("Literal \"$literal\"")
class SurfaceNotSupportedException(val surface: String) : Exception("Surface \"$surface\" is not supported.")
class LiteralNotValidException(val value: String, val iri: String) : Exception("Literal \"$value\" with \"$iri\" is not valid.")
class UndefinedPrefixException(val prefix: String) : Exception("Prefix \"$prefix\" is undefined.")

class InvalidInputException(message: String, cause: Throwable? = null) : Exception(message, cause)

class InvalidSyntax : ErrorResult()
