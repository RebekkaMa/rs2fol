package app.interfaces.results

import app.interfaces.services.TimeoutDeferred
import util.commandResult.RootError
import java.io.BufferedReader


sealed interface TheoremProverRunnerResult {
    sealed interface Success : TheoremProverRunnerResult {
        data class Ran(val output: Pair<BufferedReader, TimeoutDeferred>) : Success
    }
    sealed interface Error : TheoremProverRunnerResult, RootError {
        data class CouldNotBeStarted(val throwable: Throwable) : Error
        data class CouldNotWriteInput(val throwable: Throwable) : Error
    }
}
