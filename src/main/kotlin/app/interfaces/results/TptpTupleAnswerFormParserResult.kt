package app.interfaces.results

import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import util.commandResult.RootError

sealed interface TptpTupleAnswerFormParserResult {
    sealed interface Success : TptpTupleAnswerFormParserResult, util.commandResult.Success {
        data class Parsed(val tPTPTupleAnswerFormAnswer: TPTPTupleAnswerFormAnswer) : Success
    }

    sealed interface Error : TptpTupleAnswerFormParserResult, RootError {
        val tptpTuple: String
        data class GenericInvalidInput(override val tptpTuple: String, val throwable: Throwable) : Error
    }
}