package app.use_cases.results.commands

import util.commandResult.RootError

sealed interface RawQaAnswerToRSResult {
    sealed interface Success : util.commandResult.Success, RawQaAnswerToRSResult {
        data class WriteToLine(val res: String) : Success
        data class WriteToFile(val success: Boolean) : Success
    }

    sealed interface Error : RootError, RawQaAnswerToRSResult {
        data object NoQuestionSurface : Error
        data object MoreThanOneQuestionSurface : Error
    }
}