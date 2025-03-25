package app.interfaces.results

import entities.SZSModel
import util.commandResult.RootError

sealed interface SZSParserServiceResult {
    sealed interface Success : SZSParserServiceResult {
        data class Parsed(val szsModel: SZSModel) : Success
    }
    sealed interface Error : SZSParserServiceResult, RootError {
        data object OutputStartBeforeStatus : Error
        data object OutputEndBeforeStart : Error
        data object OutputStartBeforeEndAndStatus : Error
    }
}