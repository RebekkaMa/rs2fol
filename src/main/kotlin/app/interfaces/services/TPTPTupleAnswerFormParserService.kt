package app.interfaces.services

import app.interfaces.results.TPTPTupleAnswerFormParserResult
import util.commandResult.Result
import util.commandResult.RootError

interface TPTPTupleAnswerFormParserService {
    fun parseToEnd(answerTuple: String): Result<TPTPTupleAnswerFormParserResult.Success.Parsed, RootError>
}