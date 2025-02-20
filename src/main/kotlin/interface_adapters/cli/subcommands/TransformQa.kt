package interface_adapters.cli.subcommands

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.cli.CommonOptions
import interface_adapters.cli.util.workingDir
import interface_adapters.outputtransformer.ErrorToStringTransformer
import interface_adapters.outputtransformer.InfoToStringTransformer
import interface_adapters.outputtransformer.SolutionToStringTransformer
import use_cases.commands.TransformQaUseCase
import util.commandResult.fold
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

    private val timeLimit by option("--time-limit", "-t", help = "Time limit in seconds").long().default(120)
        .validate { it > 0 }

    private val configFile by option(
        "--config-file",
        "-cf",
        help = "Path to the configuration file"
    ).path(mustExist = true, mustBeReadable = true).default(Path(workingDir.path + "/config.json"))


    private val dEntailment by option(
        "--d-entailment",
        help = "Use D-entailment"
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

            val useCaseResult = TransformQaUseCase().invoke(
                inputStream = inputStream,
                useRdfLists = commonOptions.rdfList,
                baseIri = baseIRI,
                programName = programName,
                optionId = optionId,
                reasoningTimeLimit = timeLimit,
                outputPath = output,
                configFile = configFile,
                dEntailment = dEntailment
            )

            useCaseResult.collect { result ->

                result.fold(
                    onInfo = {
                        if (!quiet) echo(InfoToStringTransformer(it))
                    },
                    onSuccess = {
                        echo(SolutionToStringTransformer(it))
                    },
                    onFailure = {
                        echo(ErrorToStringTransformer(it), err = true)
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