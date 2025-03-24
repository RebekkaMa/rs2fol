package framework.cli

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class Rs2fol : SuspendingCliktCommand() {
    init {
        context {
            helpFormatter = { MordantHelpFormatter(it, showDefaultValues = true) }
        }
    }

    override suspend fun run() = Unit
}

class CommonOptions : OptionGroup("Standard Options") {
    val debug by option("--debug", "-d", help = "Show debugging output").flag(default = false)
    val rdfList by option(
        "--rdf-list",
        "-r",
        help = "Use the rdf:first/rdf:rest list structure to translate lists.\nIf set to \"false\", lists will be translated using FOL functions."
    ).flag(default = false, defaultForHelp = "false")
}