package framework.cli.subcommands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.path
import config.Application
import entities.rdfsurfaces.rdf_term.IRI
import framework.cli.CommonOptions
import util.commandResult.fold
import util.workingDir
import kotlin.io.path.*

class Rewrite : SuspendingCliktCommand() {
    private val commonOptions by CommonOptions()
    private val input by option(
        "--input", "-i",
        help = "Path to the RDF surface input file."
    ).path().default(Path("-"), defaultForHelp = "stdin")

    private val output by option(
        "--output", "-o",
        help = "Path to the file where the rewritten RDF surface will be written."
    ).path().default(Path("-"), defaultForHelp = "stdout")

    private val quiet by option(
        "--quiet", "-q",
        help = "Suppress non-essential output."
    ).flag(default = false)

    private val enc by option(
        help = "Enable or disable encoding. If enabled, values that are not valid in N3S or TPTP syntax are automatically encoded to ensure compatibility."
    ).switch(
        "--enc" to true,
        "--no-enc" to false
    ).default(true)

    private val dEntailment by option(
        "--d-entailment",
        help = "Enables datatype entailment. Literals with the same literal value but different lexical representations or datatypes are unified using a canonical form. Only supported for XSD datatypes"
    ).flag(default = false)

    override fun help(context: Context) =
        "Rewrites the RDF surface provided via --input, optionally applying encoding and datatype entailment, and writes the result to the specified output path."

    override suspend fun run() {

        try {
            val (inputStream, baseIRI) = when {
                input.pathString == "-" -> System.`in` to workingDir

                input.notExists() -> throw BadParameterValue(
                    currentContext.localization.pathDoesNotExist(
                        "file",
                        input.pathString
                    )
                )

                input.isReadable().not() -> throw BadParameterValue(
                    currentContext.localization.pathIsNotReadable(
                        "file",
                        input.pathString
                    )
                )

                else -> input.inputStream() to IRI.from("file://" + input.absolute().parent.invariantSeparatorsPathString + "/")
            }
            val rdfSurface = inputStream.bufferedReader().use { it.readText() }
            val result = Application.createRewriteUseCase().invoke(
                rdfSurface = rdfSurface,
                rdfList = commonOptions.rdfList,
                baseIRI = baseIRI,
                output = output,
                dEntailment = dEntailment,
                encode = enc
            )

            val infoToStringTransformerService = Application.createInfoToStringTransformerService()
            val successToStringTransformerService = Application.createCliSuccessToStringTransformerService()
            val errorToStringTransformerService = Application.createErrorToStringTransformerService()

            result.collect { res ->
                res.fold(
                    onInfo = {
                        if (!quiet) echo(infoToStringTransformerService.invoke(it))
                    },
                    onSuccess = {
                        echo(successToStringTransformerService.invoke(it))
                    },
                    onFailure = {
                        echo(errorToStringTransformerService.invoke(it, commonOptions.debug), err = true)
                    }
                )
            }
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echo(exception.stackTraceToString(), true) else echo(exception.toString(), true)
            throw ProgramResult(1)
        }
    }
}