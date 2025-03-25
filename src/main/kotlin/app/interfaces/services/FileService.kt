package app.interfaces.services

import java.nio.file.Path

interface FileService {
    fun createNewFile(path: Path, content: String): Boolean
}