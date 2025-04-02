package app.use_cases.results.commands


sealed interface TransformResult {
    sealed interface Success : util.commandResult.Success {
        val res: String

        data class WriteToLine(override val res: String) : Success
        data class WriteToFile(override val res: String, val success: Boolean) : Success
    }
}