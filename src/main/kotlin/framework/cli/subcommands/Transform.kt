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
import com.github.ajalt.clikt.parameters.types.path
import config.Application
import entities.rdfsurfaces.rdf_term.IRI
import framework.cli.CommonOptions
import framework.cli.util.workingDir
import util.commandResult.fold
import kotlin.io.path.*

class Transform : SuspendingCliktCommand() {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get RDF surface from <path>").path()
    private val output by option("--output", "-o", help = "Write output to <path>").path()
        .default(java.nio.file.Path.of("-"))

    private val consequence by option("--consequence", "-c", help = "Path to the consequence RDF surface").path()

    private val ignoreQuerySurface by option("--ignoreQuerySurface").flag(default = false, defaultForHelp = "false")

    private val quiet by option("--quiet", "-q", help = "Display less output")
        .flag(default = false)

    private val dEntailment by option(
        "--d-entailment",
        help = "If this option is activated, literals with different lexical values but the same value in the value range are mapped to a one literal with a canonical lexical value. This only applies to values of one data type. It is presumed here that the value space of the data types is disjoint."
    ).flag(default = false)

    override fun help(context: Context) = "Transforms an RDF surface (--input) to a FOL formula in TPTP format"

    override suspend fun run() {

        try {
            val (inputStream, baseIri) = when {
                input == null || input!!.pathString == "-" -> System.`in` to workingDir

                input!!.notExists() -> throw BadParameterValue(
                    currentContext.localization.pathDoesNotExist(
                        "file",
                        input!!.pathString
                    )
                )

                input!!.isReadable().not() -> throw BadParameterValue(
                    currentContext.localization.pathIsNotReadable(
                        "file",
                        input!!.pathString
                    )
                )

                else -> input!!.inputStream() to IRI.from("file://" + input!!.absolute().parent.invariantSeparatorsPathString + "/")
            }

            val rdfSurface = inputStream.bufferedReader().use { it.readText() }
            val consequenceSurface = consequence?.readText()
            val useCaseResult = Application.createTransformUseCase().invoke(
                rdfSurface = rdfSurface,
                consequenceSurface = consequenceSurface,
                baseIri = baseIri,
                useRdfLists = commonOptions.rdfList,
                ignoreQuerySurface = ignoreQuerySurface,
                outputPath = output,
                dEntailment = dEntailment,
            )

            val infoToStringTransformerService = Application.createInfoToStringTransformerService()
            val successToStringTransformerService = Application.createCliSuccessToStringTransformerService()
            val errorToStringTransformerService = Application.createErrorToStringTransformerService()

            useCaseResult.collect { result ->
                result.fold(
                    onInfo = {
                        if (!quiet) echo(infoToStringTransformerService.invoke(it))
                    },
                    onSuccess = {
                        echo(successToStringTransformerService.invoke(it))
                    },
                    onFailure = {
                        echo(errorToStringTransformerService.invoke(it), err = true)
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
