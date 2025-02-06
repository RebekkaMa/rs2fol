import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import entities.rdfsurfaces.rdf_term.IRI


val errorKeyWordTextStyle = TextStyle(color = TextColors.red)
val workingDir = IRI.from("file://" + System.getProperty("user.dir") + "/")

fun CliktCommand.echoError(string: String) {
    echo(
        errorKeyWordTextStyle("Error: ") + string,
        err = true
    )
}

fun CliktCommand.echoNonQuiet(string: String) {
    echo("%  $string")
}