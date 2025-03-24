package interfaces

import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import util.commandResult.Error
import util.commandResult.IntermediateStatus

interface TptpTupleAnswerFormParserService {
    fun parseToEnd(answerTuple: String): IntermediateStatus<TPTPTupleAnswerFormAnswer, Error>
}