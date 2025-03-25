package app.interfaces.services.presenter

interface TextStylerService {
    fun info(message: String): String
    fun error(message: String): String
    fun warning(message: String): String
    fun debug(message: String): String
    fun highlight(message: String): String
    fun bold(message: String): String
    fun underline(message: String): String
    fun boldRed(message: String): String
    fun boldGreen(message: String): String
    fun underlineBlue(message: String): String
}