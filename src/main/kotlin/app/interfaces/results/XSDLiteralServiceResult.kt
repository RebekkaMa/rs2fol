package app.interfaces.results

import util.commandResult.RootError

sealed interface XSDLiteralServiceResult {
    sealed interface Success : XSDLiteralServiceResult, util.commandResult.Success {
        data class Literal(val literal: entities.rdfsurfaces.rdf_term.Literal) : Success
    }

    sealed interface Error : XSDLiteralServiceResult, RootError {
        data class InvalidLexicalValue(val lexicalValue: String, val datatype: String) : Error
    }
}