package interface_adapters.services.vampire

import java.io.BufferedReader
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit

object StartVampireService {

    operator fun invoke(
        vampirePrompt: String,
        input: String,
        timeLimit: Long,
    ): BufferedReader? {

        val workingDir = File(System.getProperty("user.dir"))
        val vampireProcess = ProcessBuilder(*vampirePrompt.split(" ").toTypedArray())
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()

        vampireProcess
            .outputWriter()
            ?.use { it.write(input) }

        val noTimeout = vampireProcess.waitFor(timeLimit, TimeUnit.SECONDS)

        if (noTimeout.not()) {
            vampireProcess.destroy()
            if (vampireProcess.isAlive) vampireProcess.destroyForcibly()
            return null
        }

        return vampireProcess.inputReader()
    }

    fun startForConsequenceChecking(
        vampireOption: Int,
        vampireExec: Path,
        timeLimit: Long,
        folAtom: String,
        folConjecture: String
    ): List<String>? {

        val vampirePrompt =
            if (vampireOption == 0) "$vampireExec --output_mode smtcomp -t ${timeLimit + 5}s" else {
                "$vampireExec -sa discount -awr 2 -s 1 -add large -afr on -afp 1000 -afq 2.0 -anc none -gsp on -lcm predicate -nm 64 -newcnf on -nwc 5 -sac on -urr ec_only -updr off --output_mode smtcomp -t ${timeLimit + 5}s"
            }

        return StartVampireService(
            vampirePrompt = vampirePrompt,
            timeLimit = timeLimit,
            input = folAtom + System.lineSeparator() + folConjecture
        )?.readLines()
    }

    fun startForQuestionAnswering(
        vampireOption: Int,
        vampireExec: Path,
        timeLimit: Long,
        input: String,
    ): BufferedReader? {

        val vampirePrompt =
            if (vampireOption == 0) "$vampireExec -av off -qa answer_literal -om smtcomp -t ${timeLimit + 5}s" else {
                "$vampireExec -av off -sa discount -s 1 -add large -afp 4000 -afq 1.0 -anc none -gs on -gsem off -inw on -lcm reverse -lwlo on -nm 64 -nwc 1 -sas z3 -sos all -sac on -thi all -uwa all -updr off -uhcvi on -to lpo -qa answer_literal -om smtcomp -t ${timeLimit + 5}s"
            }

        return StartVampireService(
            vampirePrompt = vampirePrompt,
            timeLimit = timeLimit,
            input = input
        )
    }
}