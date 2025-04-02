package app.use_cases.results.commands

import util.commandResult.RootError

sealed interface TransformQaResult {
    sealed interface Success : util.commandResult.Success, TransformQaResult {
        data object Timeout : Success
    }

    sealed interface Error : RootError, TransformQaResult {
        data object NoQuestionSurface : Error
        data object MoreThanOneQuestionSurface : Error
    }
}