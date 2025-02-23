package use_cases.commands.subUseCase

import interface_adapters.services.theoremProver.ConfigLoader
import util.commandResult.*
import java.nio.file.Path
import kotlin.io.path.pathString


object GetTheoremProverCommandUseCase {
    operator fun invoke(
        programName: String,
        optionId: Int,
        reasoningTimeLimit: Long,
        configFile: Path
    ): IntermediateStatus<GetTheoremProverCommandSuccess, Error> {
        val configs = ConfigLoader.loadConfig(configFile.pathString)

        val programConfig = configs.programs[programName] ?: return intermediateError(
            GetTheoremProverCommandError.TheoremProverNotFound(programName)
        )

        val programExe = programConfig.exe
        val selectedOption =
            programConfig.options.find { it.optionId == optionId }
                ?: return intermediateError(
                    GetTheoremProverCommandError.TheoremProverOptionNotFound(
                        programName,
                        optionId
                    )
                )

        val flags = selectedOption.flags.map { it.replace("\${timeLimit}", "$reasoningTimeLimit") }

        return intermediateSuccess(GetTheoremProverCommandSuccess(listOf(programExe) + flags))
    }
}

data class GetTheoremProverCommandSuccess(val command: List<String>) : Success

sealed interface GetTheoremProverCommandError : Error {
    data class TheoremProverNotFound(val programName: String) : GetTheoremProverCommandError
    data class TheoremProverOptionNotFound(val programName: String, val programOption: Int) :
        GetTheoremProverCommandError
}


