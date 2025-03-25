package adapter.file

import app.interfaces.services.FileService
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writeText

class FileServiceImpl : FileService {
    override fun createNewFile(path: Path, content: String): Boolean =
        runCatching {
            path.parent?.createDirectories()
            if (path.exists().not()) path.createFile()
            path.writeText(content)
        }.isSuccess
}