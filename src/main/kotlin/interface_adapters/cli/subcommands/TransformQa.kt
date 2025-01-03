package interface_adapters.cli.subcommands

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import domain.entities.rdf_term.IRI
import domain.error.fold
import domain.use_cases.TransformQaUseCase
import echoError
import interface_adapters.cli.CommonOptions
import interface_adapters.outputtransformer.ErrorToStringTransformer
import interface_adapters.outputtransformer.SolutionToStringTransformer
import workingDir
import kotlin.io.path.*

class TransformQa :
    CliktCommand() {
    private val commonOptions by CommonOptions()
    private val input by option(
        "--input", "-i",
        help = "Get RDF surface from <path>"
    ).path()
    private val output by option(
        "--output",
        "-o",
        help = "Write the generated FOL formula (interim result) to <path>"
    ).path()


    private val vampireExecFile by option("--vampire-exec", "-e", help = "File to the Vampire executable")
        .path(mustExist = true)
        .required()

    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)

    private val vampireOption by option("--vampire-option-mode", "-v").choice("0", "1").int().default(0)

    private val timeLimit by option("--time-limit", "-t", help = "Time limit in seconds").long().default(120)
        .validate { it > 0 }

    override fun help(context: Context) = "Transforms an RDF surface to FOL and returns the results of the Vampire question answering feature as an RDF surface"

    override fun run() {
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
                vampireMode = vampireOption,
                vampireExecutable = vampireExecFile,
                vampireTimeLimit = timeLimit,
                outputPath = output
            )

            useCaseResult.fold(
                onSuccess = {
                    echo(SolutionToStringTransformer(it))
                },
                onFailure = {
                    echo(ErrorToStringTransformer(it), err = true)
                }
            )

        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}