package interface_adapters.cli.subcommands

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import domain.entities.rdf_term.IRI
import domain.error.fold
import domain.use_cases.CheckUseCase
import echoError
import interface_adapters.cli.CommonOptions
import interface_adapters.outputtransformer.ErrorToStringTransformer
import interface_adapters.outputtransformer.SolutionToStringTransformer
import workingDir
import java.nio.file.Path
import kotlin.io.path.*

class Check :
    CliktCommand(help = "Checks whether an RDF surface (--consequence) is a logical consequence of another RDF surface (--input) using the FOL-based Vampire theorem prover") {
    private val commonOptions by CommonOptions()

    private val input by option(
        "--input", "-i",
        help = "Get RDF surface from <path>"
    ).path().default(Path("-"))

    private val consequence by option(
        "--consequence", "-c",
        help = "Path to the consequence (given as RDF surface) (default: <input-parent>/out/<input-name>)"
    ).path()

    private val vampireExec by option(
        "--vampire-exec",
        "-e",
        help = "Path to the Vampire executable"
    ).path(mustExist = true).required()
    private val quiet by option("--quiet", "-q", help = "Display less output")
        .flag(default = false)
    private val output by option(
        "--output",
        "-o",
        help = "Write the generated FOL formula (interim result) to <path>"
    ).path(mustExist = false)

    private val vampireOption by option("--vampire-option-mode", "-v")
        .choice("0", "1")
        .int()
        .default(0)

    private val timeLimit by option("--time-limit", "-t", help = "Time limit in seconds")
        .long()
        .default(120)
        .validate { it > 0 }

    override fun run() {
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
                        consequence == null -> Path(input.parent.pathString + "/out/" + input.name).takeIf { it.exists() }
                            ?: Path(input.pathString + ".out").takeIf { it.exists() }
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

            val result = CheckUseCase.invoke(
                antecedent = rdfSurface,
                consequent = rdfSurfaceConsequence,
                rdfList = commonOptions.rdfList,
                baseIri = baseIri,
                outputPath = output,
                reasoningTimeLimit = timeLimit,
                vampireMode = vampireOption,
                vampireExecutable = vampireExec
            )

            result?.fold(
                onSuccess = {
                    echo(SolutionToStringTransformer().transform(it))
                },
                onFailure = {
                    echo(ErrorToStringTransformer().transform(it), err = true)
                }
            )

        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}