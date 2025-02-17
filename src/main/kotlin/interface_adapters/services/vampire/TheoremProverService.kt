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

        typedCommand.joinToString(" ").also { println(it) }


        val workingDir = File(System.getProperty("user.dir"))
        val theoremProverProcess = ProcessBuilder(*typedCommand)
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()

        theoremProverProcess
            .outputWriter()
            ?.use { it.write(input) }

        val timeout = !theoremProverProcess.waitFor(timeLimit, TimeUnit.SECONDS)

        if (timeout) {
            theoremProverProcess.destroy()
            if (theoremProverProcess.isAlive) theoremProverProcess.destroyForcibly()
            return null
        }

        return theoremProverProcess.inputReader()
    }
}