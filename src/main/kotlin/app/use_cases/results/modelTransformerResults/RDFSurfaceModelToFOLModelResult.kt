package app.use_cases.results.modelTransformerResults

import entities.fol.FOLExpression
import util.commandResult.RootError

sealed interface RDFSurfaceModelToFOLModelResult {
    sealed interface Success : RDFSurfaceModelToFOLModelResult, util.commandResult.Success {
        data class Formulas(
            val formula: FOLExpression,
            val queryFormulas: List<FOLExpression>
        ) : Success
    }

    sealed interface Error : RootError, RDFSurfaceModelToFOLModelResult {
        data class SurfaceNotSupported(val surface: String) : Error
    }
}