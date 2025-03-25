package app.use_cases.results.modelTransformerResults

import util.commandResult.RootError

sealed interface RdfSurfaceModelToTPTPModelResult {
    sealed interface Error : RootError, RdfSurfaceModelToTPTPModelResult {
        data class SurfaceNotSupported(val surface: String) : Error
    }
}