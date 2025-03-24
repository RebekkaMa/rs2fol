package app.interfaces.serviceResults

import util.commandResult.Error

sealed interface RdfSurfaceParserError : Error {
    data object BlankNodeLabelCollision : RdfSurfaceParserError
    data class UndefinedPrefix(val prefix: String) : RdfSurfaceParserError
    data class LiteralNotValid(val value: String, val iri: String) : RdfSurfaceParserError
    data class GenericInvalidInput(val throwable: Throwable) : RdfSurfaceParserError
}