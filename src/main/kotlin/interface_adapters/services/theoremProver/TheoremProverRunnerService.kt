package interface_adapters.services.theoremProver

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.IOException

typealias TimeoutDeferred = Deferred<Boolean>

object TheoremProverRunnerService {

    operator fun invoke(
        command: List<String>,
        input: String,
        timeLimit: Long
    ): Pair<BufferedReader, TimeoutDeferred> {

        val theoremProverProcess = try {
            ProcessBuilder(*command.toTypedArray())
                .directory(File(System.getProperty("user.dir")))
                .redirectErrorStream(true)
                .start()
        } catch (e: IOException) {
            throw RuntimeException("Fehler beim Starten des Theorem Provers: ${e.message}", e)
        }

        theoremProverProcess.outputWriter()?.use {
            it.write(input)
            it.flush()
        }

        // Timeout-Handling mit einer Coroutine
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

        return theoremProverProcess.inputReader() to timeout
    }
}