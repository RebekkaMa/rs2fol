package entities.rdfsurfaces.results

import util.commandResult.RootError

sealed interface RDFSurfaceResult {
    sealed interface Error : RootError, RDFSurfaceResult {
        data object TupleArityUnequalToGraffitiCount : Error
    }
}