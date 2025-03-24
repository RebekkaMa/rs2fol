package interface_adapters.services

import interface_adapters.outputtransformer.SolutionToStringTransformer
import interfaces.FileService
import util.commandResult.Success
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writeText

object FileService : FileService {
    override fun createNewFile(path: Path, content: String): Boolean =
        runCatching {
            path.parent?.createDirectories()
            if (path.exists().not()) path.createFile()
            path.writeText(content)
        }.isSuccess

    override fun createNewFile(path: Path, content: Success): Boolean =
        runCatching {
            path.parent?.createDirectories()
            if (path.exists().not()) path.createFile()
            SolutionToStringTransformer(content)?.let { path.writeText(it) }
        }.isSuccess
}