package app.interfaces.results

import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import util.commandResult.RootError

sealed interface TPTPTupleAnswerFormParserResult {
    sealed interface Success : TPTPTupleAnswerFormParserResult, util.commandResult.Success {
        data class Parsed(val tPTPTupleAnswerFormAnswer: TPTPTupleAnswerFormAnswer) : Success
    }

    sealed interface Error : TPTPTupleAnswerFormParserResult, RootError {
        val tptpTuple: String
        data class GenericInvalidInput(override val tptpTuple: String, val throwable: Throwable) : Error
    }
}