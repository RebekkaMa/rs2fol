package app.use_cases.results

import entities.SZSStatusType
import util.commandResult.RootError

sealed interface CheckResult {
    sealed interface Error : CheckResult, RootError {
        data object UnknownTheoremProverOutput : Error
        data object VampireError : Error
    }

    sealed interface Success : CheckResult, util.commandResult.Success {
        data object Consequence : Success
        data object NoConsequence : Success
        data class NotKnown(val szsStatusType: SZSStatusType) : Success
        data object Timeout : Success
    }
}