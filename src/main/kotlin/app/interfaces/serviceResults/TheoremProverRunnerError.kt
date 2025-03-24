package app.interfaces.serviceResults

import util.commandResult.RootError

sealed interface TheoremProverRunnerError : RootError {
    data class CouldNotBeStarted(val throwable: Throwable) : TheoremProverRunnerError
    data class CouldNotWriteInput(val throwable: Throwable) : TheoremProverRunnerError
}