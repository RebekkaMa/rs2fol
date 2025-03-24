package app.interfaces.serviceResults

import util.commandResult.Error

sealed interface TptpTupleAnswerFormParserError : Error {
    val tptpTuple: String

    data class GenericInvalidInput(override val tptpTuple: String, val throwable: Throwable) :
        TptpTupleAnswerFormParserError
}
