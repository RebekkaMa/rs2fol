package entities.rdfsurfaces.results

import util.commandResult.RootError

sealed interface RdfSurfaceResult {
    sealed interface Error : RootError, RdfSurfaceResult {
        data object TupleArityUnequalToGraffitiCount : Error
    }
}