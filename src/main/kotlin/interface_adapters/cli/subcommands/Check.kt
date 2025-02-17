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
import use_cases.commands.CheckUseCase
import util.commandResult.fold
import java.nio.file.Path
import kotlin.io.path.*

class Check : SuspendingCliktCommand() {
    private val commonOptions by CommonOptions()

    private val programName by option("--program", help = "Name of the program to execute").default("vampire")
    private val optionId by option("--option-id", help = "Option ID for the selected program").int().default(0)

    private val input by option(
        "--input",
        "-i",
        help = "Get RDF surface from <path>"
    ).path().default(Path("-"))

    private val consequence by option(
        "--consequence",
        "-c",
        help = "Path to the consequence (given as RDF surface) (default: <input>.out)"
    ).path()

    private val quiet by option("--quiet", "-q", help = "Display less output")
        .flag(default = false)

    private val output by option(
        "--output",
        "-o",
        help = "Write the generated FOL formula (interim result) to <path>"
    ).path(mustExist = false)

    private val timeLimit by option("--time-limit", "-t", help = "Time limit in seconds")
        .long()
        .default(120)
        .validate { it > 0 }

    override fun help(context: Context) =
        "Checks whether an RDF surface (--consequence) is a logical consequence of another RDF surface (--input) using the FOL-based Vampire theorem prover"

    override suspend fun run() {
        try {

            val computedConsequence: Path

            val (inputStream, baseIri) = when {
                input.pathString == "-" -> {
                    computedConsequence = when {
                        consequence == null -> throw BadParameterValue(
                            currentContext.localization.missingOption(
                                "The option 'consequence' must not be null if the RDF surface is entered as a stream."
                            )
                        )

                        consequence!!.notExists() -> throw BadParameterValue(
                            currentContext.localization.pathDoesNotExist(
                                "file",
                                consequence!!.pathString
                            )
                        )

                        consequence!!.isReadable().not() -> throw BadParameterValue(
                            currentContext.localization.pathIsNotReadable(
                                "file",
                                consequence!!.pathString
                            )
                        )

                        else -> consequence!!
                    }
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
                    computedConsequence = when {
                        consequence == null -> Path(input.pathString + ".out").takeIf { it.exists() }
                            ?: throw BadParameterValue(
                                currentContext.localization.pathDoesNotExist(
                                    "file",
                                    input.pathString + ".out"
                                )
                            )

                        consequence!!.notExists() -> throw BadParameterValue(
                            currentContext.localization.pathDoesNotExist(
                                "file",
                                consequence!!.pathString
                            )
                        )

                        consequence!!.isReadable().not() -> throw BadParameterValue(
                            currentContext.localization.pathIsNotReadable(
                                "file",
                                consequence!!.pathString
                            )
                        )

                        else -> consequence!!
                    }
                    input.inputStream() to IRI.from("file://" + input.absolute().parent.invariantSeparatorsPathString + "/")
                }
            }

            val rdfSurface = inputStream.bufferedReader().use { it.readText() }
            val rdfSurfaceConsequence = computedConsequence.readText()

            val result = CheckUseCase(
                antecedent = rdfSurface,
                consequent = rdfSurfaceConsequence,
                rdfList = commonOptions.rdfList,
                baseIri = baseIri,
                outputPath = output,
                reasoningTimeLimit = timeLimit,
                optionId = optionId,
                programName = programName
            )

            result.collect { res ->
                res?.fold(
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
            if (commonOptions.debug) echo(exception.stackTraceToString(), true) else echo(exception.toString(), true)
            throw ProgramResult(1)
        }
    }
}