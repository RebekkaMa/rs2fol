package app.use_cases.results.subUseCaseResults

import util.commandResult.RootError

sealed interface TPTPTupleAnswerModelToN3SResult {
    sealed interface Error : RootError, TPTPTupleAnswerModelToN3SResult {
        data class TransformationError(val affectedFormula: String? = null, val error: RootError) : Error
    }
}