package app.use_cases.commands.subUseCase

import app.interfaces.services.ConfigLoaderService
import app.use_cases.results.subUseCaseResults.GetTheoremProverCommandResult
import util.commandResult.Error
import util.commandResult.Result
import util.commandResult.error
import util.commandResult.success
import java.nio.file.Path
import kotlin.io.path.pathString


class GetTheoremProverCommandUseCase(
    private val configLoader: ConfigLoaderService,
) {
    operator fun invoke(
        programName: String,
        optionId: Int,
        reasoningTimeLimit: Long,
        configFile: Path,
    ): Result<GetTheoremProverCommandResult.Success, Error> {
        val configs = configLoader.loadConfig(configFile.pathString)

        val programConfig = configs.programs[programName] ?: return error(
            GetTheoremProverCommandResult.Error.TheoremProverNotFound(programName)
        )

        val programExe = programConfig.exe
        val selectedOption =
            programConfig.options.find { it.optionId == optionId }
                ?: return error(
                    GetTheoremProverCommandResult.Error.TheoremProverOptionNotFound(
                        programName,
                        optionId
                    )
                )

        val flags = selectedOption.flags.map { it.replace("\${timeLimit}", "$reasoningTimeLimit") }

        return success(GetTheoremProverCommandResult.Success(listOf(programExe) + flags))
    }
}


