package app.use_cases.results.subUseCaseResults

import util.commandResult.RootError

sealed interface GetTheoremProverCommandResult {
    data class Success(val command: List<String>) : util.commandResult.Success, GetTheoremProverCommandResult

    sealed interface Error : RootError {
        data class TheoremProverNotFound(val programName: String) : Error
        data class TheoremProverOptionNotFound(val programName: String, val programOption: Int) : Error
    }
}