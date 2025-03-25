package app.interfaces.services

import app.interfaces.results.SZSParserServiceResult
import kotlinx.coroutines.flow.Flow
import util.commandResult.Result
import util.commandResult.RootError
import java.io.BufferedReader

interface SZSParserService {
    fun parse(bufferedReader: BufferedReader): Flow<Result<SZSParserServiceResult.Success.Parsed, RootError>>
}