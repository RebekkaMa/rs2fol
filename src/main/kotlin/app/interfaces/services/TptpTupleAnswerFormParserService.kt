package app.interfaces.services

import app.interfaces.results.TptpTupleAnswerFormParserResult
import util.commandResult.Result

interface TptpTupleAnswerFormParserService {
    fun parseToEnd(answerTuple: String): Result<TptpTupleAnswerFormParserResult.Success.Parsed, TptpTupleAnswerFormParserResult.Error>
}