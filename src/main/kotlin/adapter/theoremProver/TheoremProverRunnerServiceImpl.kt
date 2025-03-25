package adapter.theoremProver

import app.interfaces.results.TheoremProverRunnerResult
import app.interfaces.services.TheoremProverRunnerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import util.commandResult.Result
import util.commandResult.RootError
import util.commandResult.error
import util.commandResult.success
import java.io.File
import java.io.IOException

class TheoremProverRunnerServiceImpl : TheoremProverRunnerService {

    override operator fun invoke(
        command: List<String>,
        input: String,
        timeLimit: Long
    ): Result<TheoremProverRunnerResult.Success.Ran, RootError> {

        val theoremProverProcess = try {
            ProcessBuilder(*command.toTypedArray())
                .directory(File(System.getProperty("user.dir")))
                .redirectErrorStream(true)
                .start()
        } catch (e: IOException) {
            return error(TheoremProverRunnerResult.Error.CouldNotBeStarted(e))
        }

        kotlin.runCatching {
            theoremProverProcess.outputWriter()?.use {
                it.write(input)
                it.flush()
            }
        }.onFailure {
            return error(TheoremProverRunnerResult.Error.CouldNotWriteInput(it))
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

        return success(TheoremProverRunnerResult.Success.Ran(theoremProverProcess.inputReader() to timeout))
    }
}
