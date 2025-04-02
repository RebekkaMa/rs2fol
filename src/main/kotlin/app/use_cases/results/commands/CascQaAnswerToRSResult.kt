package app.use_cases.results.commands

import util.commandResult.RootError

sealed interface CascQaAnswerToRSResult {
    sealed interface Success : util.commandResult.Success, CascQaAnswerToRSResult {
        data class WriteToLine(val res: String) : Success
        data class WriteToFile(val success: Boolean) : Success
    }

    sealed interface Error : RootError, CascQaAnswerToRSResult {
        data object NoQuestionSurface : Error
        data object MoreThanOneQuestionSurface : Error
    }
}