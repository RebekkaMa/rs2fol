package app.interfaces.services

import entities.program.ProgramConfig

interface ConfigLoaderService {
    fun loadConfig(filePath: String): ProgramConfig
}