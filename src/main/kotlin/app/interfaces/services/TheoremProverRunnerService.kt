package app.interfaces.services

import app.interfaces.results.TheoremProverRunnerResult
import kotlinx.coroutines.Deferred
import util.commandResult.Result
import util.commandResult.RootError

typealias TimeoutDeferred = Deferred<Boolean>

interface TheoremProverRunnerService {
    operator fun invoke(
        command: List<String>,
        input: String,
        timeLimit: Long
    ): Result<TheoremProverRunnerResult.Success.Ran, RootError>
}