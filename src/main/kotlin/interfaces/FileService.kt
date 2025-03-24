package interfaces

import util.commandResult.Success
import java.nio.file.Path

interface FileService {
    fun createNewFile(path: Path, content: String): Boolean
    fun createNewFile(path: Path, content: Success): Boolean
}