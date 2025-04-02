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

class QaAnswerToRS : SuspendingCliktCommand() {
    private val commonOptions by CommonOptions()

    private val input by option(
        "--input", "-i",
        help = "Path to the TPTP answer tuple input file."
    ).path().default(Path("-"), "stdin")

    private val output by option(
        "--output", "-o",
        help = "Path to the file where the resulting RDF surface will be written."
    ).path(mustExist = false).default(Path("-"), "stdout")

    private val questionSurface by option(
        "--q-surface", "-q",
        help = "Path to the negative answer surface (question) used for reconstructing the answer."
    ).path(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).required()

    private val quiet by option(
        "--quiet",
        help = "Suppress non-essential output."
    ).flag(default = false)

    private val inputType by option(
        "--input-type", "-it",
        help = "Format of the input file: 'szs' (default) or 'raw'."
    ).choice("raw", "szs").default("szs")

    override fun help(context: Context) =
        "Transforms a TPTP answer tuple provided via --input into an RDF surface using the negative answer surface specified via --q-surface. Supports raw or SZS-wrapped input."
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

            val rdfSurfaces = questionSurface.readText()

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
                        echo(errorToStringTransformerService.invoke(it, commonOptions.debug), err = true)
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