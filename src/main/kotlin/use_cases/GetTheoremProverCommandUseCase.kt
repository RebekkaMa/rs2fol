package use_cases

import interface_adapters.services.ConfigLoader
import util.commandResult.*
import java.nio.file.Paths


object GetTheoremProverCommandUseCase {
    operator fun invoke(
        programName: String,
        optionId: Int,
        reasoningTimeLimit: Long
    ): IntermediateStatus<GetTheoremProverCommandSuccess, Error> {
        val configPath = Paths.get(".", "programConfig.json").toString()
        val configs = ConfigLoader.loadConfig(configPath)

        val programConfig = configs.programs.getOrElse(programName) {
            return intermediateError(
                GetTheoremProverCommandError.ProgramNotFound(programName)
            )
        }
        val programExe = programConfig.exe
        val selectedOption =
            programConfig.options.find { it.optionId == optionId }
                ?: run {
                    return intermediateError(
                        GetTheoremProverCommandError.ProgramOptionNotFound(
                            programName,
                            optionId
                        )
                    )
                }

        val flags = selectedOption.flags.map { flag ->
            flag.replace("\${timeLimit}", "$reasoningTimeLimit")

        }
        return intermediateSuccess(GetTheoremProverCommandSuccess(listOf(programExe) + flags))
    }
}

data class GetTheoremProverCommandSuccess(val command: List<String>) : Success

sealed interface GetTheoremProverCommandError : Error {
    data class ProgramNotFound(val programName: String) : GetTheoremProverCommandError
    data class ProgramOptionNotFound(val programName: String, val programOption: Int) : GetTheoremProverCommandError
}