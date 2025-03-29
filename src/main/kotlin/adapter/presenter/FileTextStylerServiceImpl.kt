package adapter.presenter

import app.interfaces.services.presenter.TextStylerService

class FileTextStylerServiceImpl : TextStylerService {

    override fun info(message: String) = message

    override fun error(message: String) = message

    override fun warning(message: String) = message

    override fun debug(message: String) = message

    override fun highlight(message: String) = message

    override fun bold(message: String) = message

    override fun underline(message: String) = message

    override fun boldRed(message: String) = message

    override fun boldGreen(message: String) = message

    override fun underlineBlue(message: String) = message

    override fun boldYellow(message: String): String = message
}