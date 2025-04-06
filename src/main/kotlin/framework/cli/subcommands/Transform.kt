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

class Transform : SuspendingCliktCommand() {
    private val commonOptions by CommonOptions()

    private val input by option(
        "--input", "-i",
        help = "Path to the RDF surface input file. It will be transformed into a TPTP annotated first-order formula of type axiom."
    ).path().default(Path("-"), defaultForHelp = "stdin")

    private val output by option(
        "--output", "-o",
        help = "Path to the output file"
    ).path().default(Path("-"), defaultForHelp = "stdout")

    private val consequence by option(
        "--consequence", "-c",
        help = "Path to the RDF surface representing the expected consequence. It will be transformed into a TPTP annotated first-order formula of type conjecture."
    ).path()

    private val ignoreNegativeAnswerSurface by option(
        "--ignoreNegativeAnswerSurface",
        help = "Ignore all negative answer surfaces that are either on the default surface or not embedded in any other surface."
    ).flag(default = false, defaultForHelp = "false")

    private val quiet by option(
        "--quiet", "-q",
        help = "Suppress non-essential output"
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
        "Transforms an RDF surface given via --input into an TPTP annotated first-order formula of type axiom. If a consequence is provided via --consequence, it will additionally be transformed into a TPTP annotated first-order formula of type conjecture."

    override suspend fun run() {

        try {
            val (inputStream, baseIri) = when {
                input.pathString == "-" -> System.`in` to workingDir

                input.notExists() -> throw BadParameterValue(
                    currentContext.localization.pathDoesNotExist(
                        "file",
                        input!!.pathString
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
            val consequenceSurface = consequence?.readText()
            val useCaseResult = Application.createTransformUseCase().invoke(
                rdfSurface = rdfSurface,
                consequenceSurface = consequenceSurface,
                baseIri = baseIri,
                useRdfLists = commonOptions.rdfList,
                ignoreQuerySurface = ignoreNegativeAnswerSurface,
                outputPath = output,
                dEntailment = dEntailment,
                encode = enc
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
