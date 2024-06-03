package interface_adapters.cli.subcommands

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import domain.entities.rdf_term.IRI
import domain.error.fold
import domain.use_cases.TransformUseCase
import echoError
import interface_adapters.cli.CommonOptions
import interface_adapters.outputtransformer.ErrorToStringTransformer
import interface_adapters.outputtransformer.SolutionToStringTransformer
import workingDir
import kotlin.io.path.*

class Transform : CliktCommand(help = "Transforms an RDF surface (--input) to a FOL formula in TPTP format") {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get RDF surface from <path>").path()
    private val output by option("--output", "-o", help = "Write output to <path>").path().default(java.nio.file.Path.of("-"))

    private val ignoreQuerySurface by option("--ignoreQuerySurface").flag(default = false, defaultForHelp = "false")

    override fun run() {

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
            val useCaseResult = TransformUseCase(
                rdfSurface = rdfSurface,
                baseIri = baseIri,
                useRdfLists = commonOptions.rdfList,
                ignoreQuerySurface = ignoreQuerySurface,
                outputPath = output
            )

            useCaseResult.fold(
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
