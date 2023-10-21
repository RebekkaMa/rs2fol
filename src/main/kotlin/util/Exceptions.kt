package util

import com.github.h0tk3y.betterParse.parser.ErrorResult

const val generalParseErrorString = "Failed to parse RDF surface. Please check the syntax."

class TransformerException(message: String, cause: Throwable? = null) : Exception(message, cause)
class NotSupportedException(message: String, cause: Throwable? = null) : Exception(message, cause)
class InvalidInputException(message: String, cause: Throwable? = null) : Exception(message, cause)

class InvalidSyntax : ErrorResult()
