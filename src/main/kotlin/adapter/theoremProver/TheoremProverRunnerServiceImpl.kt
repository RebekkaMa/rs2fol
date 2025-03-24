package adapter.theoremProver

import app.interfaces.serviceResults.TheoremProverRunnerError
import app.interfaces.services.TheoremProverRunnerService
import kotlinx.coroutines.*
import util.commandResult.IntermediateStatus
import util.commandResult.RootError
import util.commandResult.intermediateError
import util.commandResult.intermediateSuccess
import java.io.BufferedReader
import java.io.File
import java.io.IOException

typealias TimeoutDeferred = Deferred<Boolean>

class TheoremProverRunnerServiceImpl : TheoremProverRunnerService {

    override operator fun invoke(
        command: List<String>,
        input: String,
        timeLimit: Long
    ): IntermediateStatus<Pair<BufferedReader, TimeoutDeferred>, RootError> {

        val theoremProverProcess = try {
            ProcessBuilder(*command.toTypedArray())
                .directory(File(System.getProperty("user.dir")))
                .redirectErrorStream(true)
                .start()
        } catch (e: IOException) {
            return intermediateError(TheoremProverRunnerError.CouldNotBeStarted(e))
        }

        kotlin.runCatching {
            theoremProverProcess.outputWriter()?.use {
                it.write(input)
                it.flush()
            }
        }.onFailure {
            return intermediateError(TheoremProverRunnerError.CouldNotWriteInput(it))
        }

        val timeout = CoroutineScope(Dispatchers.IO).async {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < timeLimit * 1000) {
                if (!theoremProverProcess.isAlive) return@async false
                delay(1000)
            }
            theoremProverProcess.destroy()
            if (theoremProverProcess.isAlive) theoremProverProcess.destroyForcibly()
            return@async true
        }

        return intermediateSuccess(theoremProverProcess.inputReader() to timeout)
    }
}
