package app.use_cases.results.commands

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
        data object Contradiction : Success
        data object Satisfiable : Success
        data object Unsatisfiable : Success
        data class NotKnown(val szsStatusType: SZSStatusType) : Success
        data object Timeout : Success
    }
}