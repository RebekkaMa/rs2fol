package interfaces

import kotlinx.coroutines.Deferred
import util.commandResult.IntermediateStatus
import util.commandResult.RootError
import java.io.BufferedReader

typealias TimeoutDeferred = Deferred<Boolean>

interface TheoremProverRunnerService {
    operator fun invoke(
        command: List<String>,
        input: String,
        timeLimit: Long
    ): IntermediateStatus<Pair<BufferedReader, TimeoutDeferred>, RootError>
}