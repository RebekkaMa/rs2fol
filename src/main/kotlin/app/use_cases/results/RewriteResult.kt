package app.use_cases.results

sealed interface RewriteResult {
    sealed interface Success : util.commandResult.Success, RewriteResult {
        data class WriteToLine(val res: String) : Success
        data class WriteToFile(val success: Boolean) : Success
    }
}