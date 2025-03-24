package interfaces

import interface_adapters.services.theoremProver.ProgramsConfig

interface ConfigLoaderService {
    fun loadConfig(filePath: String): ProgramsConfig
}