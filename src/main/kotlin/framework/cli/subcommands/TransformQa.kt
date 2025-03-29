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

class TransformQa :
    SuspendingCliktCommand() {
    private val commonOptions by CommonOptions()

    private val programName by option("--program", help = "Name of the program to execute").default("vampire")
    private val optionId by option("--option-id", help = "Option ID for the selected program").int().default(0)

    private val input by option(
        "--input", "-i",
        help = "Get RDF surface from <path>"
    ).path()

    private val output by option(
        "--output",
        "-o",
        help = "Write the generated FOL formula (interim result) to <path>"
    ).path()

    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)

    private val timeLimit by option("--time-limit", "-t", help = "Time limit in seconds")
        .long()
        .default(120)
        .validate { require(it > 0) { "Time limit must be greater than 0!" } }

    private val configFile by option(
        "--config-file",
        "-cf",
        help = "Path to the configuration file"
    ).path(mustExist = true, mustBeReadable = true).required()

    private val disEnc by option(
        "--disEnc",
        help = "Disable encoding. If this option is deactivated, values that correspond to the N3S or TPTP syntax will be encoded."
    ).flag(default = false)

    private val dEntailment by option(
        "--d-entailment",
        help = "If this option is activated, literals with different lexical values but the same value in the value space are mapped to one literal with a canonical lexical value and datatype. This is only supported for XSD datatypes."
    ).flag(default = false)


    override fun help(context: Context) =
        "Transforms an RDF surface to FOL and returns the results of the Vampire question answering feature as an RDF surface"

    override suspend fun run() {
        try {
            val baseIRI: IRI

            val inputStream = when {
                input == null || input!!.pathString == "-" -> {
                    baseIRI = workingDir
                    System.`in`
                }

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

                else -> {
                    baseIRI = IRI.from("file://" + input!!.absolute().parent.invariantSeparatorsPathString + "/")
                    input!!.inputStream()
                }
            }

            val useCaseResult = Application.createTransformQaUseCase().invoke(
                inputStream = inputStream,
                useRdfLists = commonOptions.rdfList,
                baseIri = baseIRI,
                programName = programName,
                optionId = optionId,
                reasoningTimeLimit = timeLimit,
                outputPath = output,
                configFile = configFile,
                dEntailment = dEntailment,
                encode = !disEnc
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
            if (commonOptions.debug) echo(exception.stackTraceToString(), err = true) else echo(
                exception.toString(),
                err = true
            )
            throw ProgramResult(1)
        }
    }
}