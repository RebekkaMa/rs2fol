package app.use_cases.results

import util.commandResult.RootError

sealed interface CascQaAnswerToRsResult {
    sealed interface Success : util.commandResult.Success, CascQaAnswerToRsResult {
        data class WriteToLine(val res: String) : Success
        data class WriteToFile(val success: Boolean) : Success
    }

    sealed interface Error : RootError, CascQaAnswerToRsResult {
        data object NoQuestionSurface : Error
        data object MoreThanOneQuestionSurface : Error
    }
}