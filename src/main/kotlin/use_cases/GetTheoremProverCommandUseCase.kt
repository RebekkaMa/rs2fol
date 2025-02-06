package use_cases

import interface_adapters.services.ConfigLoader
import util.error.Error
import util.error.Result
import util.error.Success
import java.nio.file.Paths


object GetTheoremProverCommandUseCase {
    operator fun invoke(
        programName: String,
        optionId: Int,
        reasoningTimeLimit: Long
    ): Result<GetTheoremProverCommandSuccess, Error> {
        val configPath = Paths.get(".", "programConfig.json").toString()
        val configs = ConfigLoader.loadConfig(configPath)

        val programConfig = configs.programs.getOrElse(programName) {
            return util.error.error(
                GetTheoremProverCommandError.ProgramNotFound(programName)
            )
        }
        val programExe = programConfig.exe
        val selectedOption =
            programConfig.options.find { it.optionId == optionId }
                ?: run {
                    return util.error.error(
                        GetTheoremProverCommandError.ProgramOptionNotFound(
                            programName,
                            optionId
                        )
                    )
                }

        val flags = selectedOption.flags.map { flag ->
            flag.replace("\${timeLimit}", "$reasoningTimeLimit")

        }
        return util.error.success(GetTheoremProverCommandSuccess(listOf(programExe) + flags))
    }
}

data class GetTheoremProverCommandSuccess(val command: List<String>) : Success

sealed interface GetTheoremProverCommandError : Error {
    data class ProgramNotFound(val programName: String) : GetTheoremProverCommandError
    data class ProgramOptionNotFound(val programName: String, val programOption: Int) : GetTheoremProverCommandError
}