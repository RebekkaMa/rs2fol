package util

import com.github.h0tk3y.betterParse.parser.ErrorResult

class SurfaceNotSupportedException(val surface: String) : Exception("Surface \"$surface\" is not supported.")
class LiteralNotValidException(val value: String, val iri: String) : Exception("Literal \"$value\" with \"$iri\" is not valid.")
class UndefinedPrefixException(val prefix: String) : Exception("Prefix \"$prefix\" is undefined.")

class InvalidSyntax : ErrorResult()
