package interface_adapters.services.theoremProver

import interfaces.ConfigLoaderService
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

object ConfigLoaderServiceImpl : ConfigLoaderService {
    override fun loadConfig(filePath: String): ProgramsConfig {
        val json = Json { ignoreUnknownKeys = true }

        val jsonContent = File(filePath).readText()
        return json.decodeFromString(ProgramsConfig.serializer(), jsonContent)
    }
}