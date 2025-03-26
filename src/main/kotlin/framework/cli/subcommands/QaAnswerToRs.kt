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
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import config.Application
import entities.rdfsurfaces.rdf_term.IRI
import framework.cli.CommonOptions
import util.commandResult.fold
import util.workingDir
import kotlin.io.path.*

class QaAnswerToRs : SuspendingCliktCommand() {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get TPTP answer tuple from <path>").path().default(Path("-"))
    private val output by option("--output", "-o", help = "Write output to <path>").path(mustExist = false)
        .default(Path("-"))

    private val querySurface by option(
        "--q-surface",
        "-q",
        help = "Get RDF query or question surface from <path>"
    ).path(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).required()

    private val quiet by option("--quiet", help = "Display less output")
        .flag(default = false)

    private val inputType by option("--input-type", "-it").choice("raw", "szs").default("szs")

    override fun help(context: Context) =
        "Transforms a TPTP tuple answer (--input) into an RDF surface using a specified RDF query or question surface (--q-surface)"

    override suspend fun run() {

        try {
            val (inputStream, baseIri) = when {
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

            val rdfSurfaces = querySurface.readText()

            val result = if (inputType == "raw") {
                Application.createRawQaAnswerToRsUseCase().invoke(
                    inputStream = inputStream,
                    rdfSurface = rdfSurfaces,
                    baseIri = baseIri,
                    outputPath = output,
                    rdfList = commonOptions.rdfList,
                )
            } else {
                Application.createCascQaAnswerToRsUseCase().invoke(
                    inputStream = inputStream,
                    rdfSurface = rdfSurfaces,
                    baseIri = baseIri,
                    outputPath = output,
                    rdfList = commonOptions.rdfList,
                )
            }

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
                        echo(errorToStringTransformerService.invoke(it), err = true)
                    }
                )
            }

        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echo(exception.stackTraceToString(), true) else echo(exception.message, true)
            throw ProgramResult(1)
        }
    }
}