package adapter.presenter

import app.interfaces.services.presenter.TextStylerService
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle

class AjaltMordantTextStylerServiceImpl : TextStylerService {

    override fun info(message: String) = TextColors.green(message)

    override fun error(message: String) = TextColors.red(message)

    override fun warning(message: String) = TextColors.yellow(message)

    override fun debug(message: String) = TextColors.cyan(message)

    override fun highlight(message: String) = TextColors.magenta(message)

    override fun bold(message: String) = TextStyle(bold = true)(message)

    override fun underline(message: String) = TextStyle(underline = true)(message)

    override fun boldRed(message: String) = TextStyle(color = TextColors.red, bold = true)(message)

    override fun boldGreen(message: String) = TextStyle(color = TextColors.green, bold = true)(message)

    override fun boldYellow(message: String) = TextStyle(color = TextColors.brightYellow, bold = true)(message)

    override fun underlineBlue(message: String) = TextStyle(color = TextColors.blue, underline = true)(message)

}