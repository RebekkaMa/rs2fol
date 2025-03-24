package adapter.theoremProver

import app.interfaces.services.ConfigLoaderService
import entities.program.ProgramConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ProgramOption(
    val optionId: Int,
    val flags: List<String>
)

@Serializable
data class Program(
    val exe: String,
    val options: List<ProgramOption>
)

@Serializable
data class ProgramsConfig(
    val programs: Map<String, Program>
)

class ConfigLoaderServiceImpl : ConfigLoaderService {
    override fun loadConfig(filePath: String): ProgramConfig {
        val json = Json { ignoreUnknownKeys = true }

        val jsonContent = File(filePath).readText()
        val programsConfig = json.decodeFromString(ProgramsConfig.serializer(), jsonContent)

        val programMap = programsConfig.programs.mapValues { entry ->
            entities.program.Program(
                exe = entry.value.exe,
                options = entry.value.options.map { option ->
                    entities.program.ProgramOption(
                        optionId = option.optionId,
                        flags = option.flags
                    )
                }
            )
        }

        return ProgramConfig(programs = programMap)
    }
}