package app.interfaces.services

import app.interfaces.results.TptpTupleAnswerFormParserResult
import util.commandResult.Result
import util.commandResult.RootError

interface TptpTupleAnswerFormParserService {
    fun parseToEnd(answerTuple: String): Result<TptpTupleAnswerFormParserResult.Success.Parsed, RootError>
}