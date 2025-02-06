package interface_adapters.services.vampire

import java.io.BufferedReader
import java.io.File
import java.util.concurrent.TimeUnit

object TheoremProverService {

    operator fun invoke(
        command: List<String>,
        input: String,
        timeLimit: Long,
    ): BufferedReader? {

        val typedCommand = command.toTypedArray()

        val workingDir = File(System.getProperty("user.dir"))
        val vampireProcess = ProcessBuilder(*typedCommand)
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()

        vampireProcess
            .outputWriter()
            ?.use { it.write(input) }

        val timeout = !vampireProcess.waitFor(timeLimit, TimeUnit.SECONDS)

        if (timeout) {
            vampireProcess.destroy()
            if (vampireProcess.isAlive) vampireProcess.destroyForcibly()
            return null
        }

        return vampireProcess.inputReader()
    }
}