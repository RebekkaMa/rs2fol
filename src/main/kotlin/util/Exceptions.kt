package util

const val generalParseErrorString = "Failed to parse RDF surfaces graph. Please check the syntax."

class TransformerException(message: String, cause: Throwable? = null) : Exception(message, cause)
class NotSupportedException(message: String, cause: Throwable? = null) : Exception(message, cause)
class InvalidInputException(message: String, cause: Throwable? = null) : Exception(message, cause)
