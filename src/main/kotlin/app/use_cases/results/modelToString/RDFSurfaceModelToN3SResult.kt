package app.use_cases.results.modelToString

import util.commandResult.RootError

sealed interface RDFSurfaceModelToN3SResult {
    sealed interface Error : RootError, RDFSurfaceModelToN3SResult {
        data class LiteralTransformationError(val value: String, val iri: String) : Error
    }
}