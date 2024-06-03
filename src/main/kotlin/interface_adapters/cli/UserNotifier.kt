package interface_adapters.cli

object Notifier {
    // ANSI Escape Codes für Farben
    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"
    private const val BLUE = "\u001B[34m"
    private const val PURPLE = "\u001B[35m"
    private const val CYAN = "\u001B[36m"

    // ANSI Escape Codes für Formatierungen
    private const val BOLD = "\u001B[1m"
    private const val UNDERLINE = "\u001B[4m"

    fun info(message: String, trailingNewline: Boolean = true) {
        printMessage("$GREEN$message$RESET", trailingNewline)
    }

    fun error(message: String, trailingNewline: Boolean = true) {
        printMessage("$RED$message$RESET", trailingNewline)
    }

    fun warning(message: String, trailingNewline: Boolean = true) {
        printMessage("$YELLOW$message$RESET", trailingNewline)
    }

    fun debug(message: String, trailingNewline: Boolean = true) {
        printMessage("$CYAN$message$RESET", trailingNewline)
    }

    fun highlight(message: String, trailingNewline: Boolean = true) {
        printMessage("$PURPLE$message$RESET", trailingNewline)
    }

    fun bold(message: String, trailingNewline: Boolean = true) {
        printMessage("$BOLD$message$RESET", trailingNewline)
    }

    fun underline(message: String, trailingNewline: Boolean = true) {
        printMessage("$UNDERLINE$message$RESET", trailingNewline)
    }

    fun boldRed(message: String, trailingNewline: Boolean = true) {
        printMessage("$BOLD$RED$message$RESET", trailingNewline)
    }

    fun boldGreen(message: String, trailingNewline: Boolean = true) {
        printMessage("$BOLD$GREEN$message$RESET", trailingNewline)
    }

    fun underlineBlue(message: String, trailingNewline: Boolean = true) {
        printMessage("$UNDERLINE$BLUE$message$RESET", trailingNewline)
    }

    private fun printMessage(message: String, trailingNewline: Boolean) {
        if (trailingNewline) {
            println(message)
        } else {
            print(message)
        }
    }
}