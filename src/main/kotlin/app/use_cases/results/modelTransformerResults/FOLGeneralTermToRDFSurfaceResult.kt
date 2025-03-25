package app.use_cases.results.modelTransformerResults

import util.commandResult.RootError

sealed interface FOLGeneralTermToRDFSurfaceResult {
    sealed interface Error : RootError, FOLGeneralTermToRDFSurfaceResult {
        data class InvalidFunctionOrPredicate(val element: String) : Error
        data class InvalidElement(val element: String) : Error
    }
}