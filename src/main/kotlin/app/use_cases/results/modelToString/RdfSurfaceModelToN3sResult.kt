package app.use_cases.results.modelToString

import util.commandResult.RootError

sealed interface RdfSurfaceModelToN3sResult {
    sealed interface Error : RootError, RdfSurfaceModelToN3sResult {
        data class LiteralTransformationError(val value: String, val iri: String) : Error
    }
}