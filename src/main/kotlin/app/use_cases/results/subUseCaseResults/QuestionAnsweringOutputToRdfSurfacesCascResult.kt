package app.use_cases.results.subUseCaseResults

import util.commandResult.RootError

sealed interface QuestionAnsweringOutputToRdfSurfacesCascResult {
    sealed interface Error : RootError, QuestionAnsweringOutputToRdfSurfacesCascResult {
        data class AnswerTupleTransformation(val affectedFormula: String? = null, val error: Error) : Error
    }

    sealed interface Success : util.commandResult.Success, QuestionAnsweringOutputToRdfSurfacesCascResult {
        data object Refutation : Success
        data object NothingFound : Success
        data class Answer(val data: String) : Success
    }
}