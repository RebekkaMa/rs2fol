package framework.cli.subcommands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import config.Application
import entities.rdfsurfaces.rdf_term.IRI
import framework.cli.CommonOptions
import util.commandResult.fold
import util.workingDir
import kotlin.io.path.*

class Check : SuspendingCliktCommand() {
    private val commonOptions by CommonOptions()

    private val programName by option(
        "--program",
        help = "Name of the external reasoning program to execute, as specified in the configuration file (e.g., vampire)."
    ).default("vampire")

    private val optionId by option(
        "--option-id",
        help = "Numeric identifier for a configuration entry of the selected program, as defined in the configuration file."
    ).int().default(0)

    private val input by option(
        "--input", "-i",
        help = "Path to the RDF surface input file. It will be transformed into a TPTP annotated first-order formula of type axiom."
    ).path().default(Path("-"), "stdin")

    private val consequence by option(
        "--consequence", "-c",
        help = "Path to the RDF surface representing the expected consequence. It will be transformed into a TPTP annotated first-order formula of type conjecture."
    ).path(mustExist = true, mustBeReadable = true)

    private val quiet by option(
        "--quiet", "-q",
        help = "Suppress non-essential output."
    ).flag(default = false)

    private val output by option(
        "--output", "-o",
        help = "Path to the file where the generated FOL formula (intermediate result) will be written."
    ).path(mustExist = false)

    private val timeLimit by option(
        "--time-limit", "-t",
        help = "Time limit for program execution in seconds."
    ).long()
        .default(120)
        .validate { require(it > 0) { "Time limit must be greater than 0!" } }

    private val configFile by option(
        "--config",
        help = "Path to the configuration file specifying available theorem provers and their options."
    ).path(mustExist = true, mustBeReadable = true).required()

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
        "Checks whether the RDF surface provided via --consequence is a logical consequence of the RDF surface given via --input, using the specified theorem prover. Additional options control encoding, datatype entailment, and execution behavior."
    override suspend fun run() {
        try {

            val (inputStream, baseIri) = when {
                input.pathString == "-" -> {
                    System.`in` to workingDir
                }

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

                else -> {
                    input.inputStream() to IRI.from("file://" + input.absolute().parent.invariantSeparatorsPathString + "/")
                }
            }

            val rdfSurface = inputStream.bufferedReader().use { it.readText() }
            val rdfSurfaceConsequence = consequence?.readText()

            val result = Application.createCheckUseCase().invoke(
                antecedent = rdfSurface,
                consequent = rdfSurfaceConsequence,
                rdfList = commonOptions.rdfList,
                baseIri = baseIri,
                outputPath = output,
                reasoningTimeLimit = timeLimit,
                optionId = optionId,
                programName = programName,
                configFile = configFile,
                dEntailment = dEntailment,
                encode = enc,
            )

            val infoToStringTransformerService = Application.createInfoToStringTransformerService()
            val successToStringTransformerService = Application.createCliSuccessToStringTransformerService()
            val errorToStringTransformerService = Application.createErrorToStringTransformerService()

            result.collect { res ->
                res?.fold(
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