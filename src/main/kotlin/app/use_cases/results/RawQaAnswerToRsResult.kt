package app.use_cases.results

import util.commandResult.RootError

sealed interface RawQaAnswerToRsResult {
    sealed interface Success : util.commandResult.Success, RawQaAnswerToRsResult {
        data class WriteToLine(val res: String) : Success
        data class WriteToFile(val success: Boolean) : Success
    }

    sealed interface Error : RootError,RawQaAnswerToRsResult {
        data object NoQuestionSurface : Error
        data object MoreThanOneQuestionSurface : Error
    }
}