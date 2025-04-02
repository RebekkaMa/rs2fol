package app.use_cases.results.subUseCaseResults

import util.commandResult.RootError

sealed interface QuestionAnsweringOutputToRDFSurfacesCascResult {
    sealed interface Error : RootError, QuestionAnsweringOutputToRDFSurfacesCascResult {
        data class AnswerTupleTransformation(val affectedFormula: String? = null, val error: Error) : Error
    }

    sealed interface Success : util.commandResult.Success, QuestionAnsweringOutputToRDFSurfacesCascResult {
        data object Refutation : Success
        data object NothingFound : Success
        data class Answer(val data: String) : Success
    }
}