package framework.cli.outputtransformer

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle

object TextStyler {

    fun info(message: String) = TextColors.green(message)

    fun error(message: String) = TextColors.red(message)

    fun test(message: String) = TextStyle()

    fun warning(message: String) = TextColors.yellow(message)

    fun debug(message: String) = TextColors.cyan(message)

    fun highlight(message: String) = TextColors.magenta(message)

    fun bold(message: String) = TextStyle(bold = true)(message)

    fun underline(message: String) = TextStyle(underline = true)(message)

    fun boldRed(message: String) = TextStyle(color = TextColors.red, bold = true)(message)

    fun boldGreen(message: String) = TextStyle(color = TextColors.green, bold = true)(message)

    fun underlineBlue(message: String) = TextStyle(color = TextColors.blue, underline = true)(message)

}