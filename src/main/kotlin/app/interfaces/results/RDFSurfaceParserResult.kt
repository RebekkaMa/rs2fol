package app.interfaces.results

import entities.rdfsurfaces.PositiveSurface
import util.commandResult.RootError

sealed interface RDFSurfaceParserResult {
    sealed interface Success : RDFSurfaceParserResult, util.commandResult.Success {
        data class Parsed(val positiveSurface: PositiveSurface) : Success
    }
    sealed interface Error : RDFSurfaceParserResult, RootError {
        data object BlankNodeLabelCollision : Error
        data class UndefinedPrefix(val prefix: String) : Error
        data class LiteralNotValid(val value: String, val iri: String) : Error
        data class GenericInvalidInput(val cause: String) : Error
        data class SurfaceNotSupported(val surface: String) : Error
    }
}