package app.interfaces.serviceResults

import util.commandResult.Error

sealed interface SZSParserServiceError : Error {
    data object OutputStartBeforeStatus : SZSParserServiceError
    data object OutputEndBeforeStart : SZSParserServiceError
    data object OutputStartBeforeEndAndStatus : SZSParserServiceError
}