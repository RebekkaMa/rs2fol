package interface_adapters.cli.subcommands

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import domain.entities.rdf_term.IRI
import domain.error.fold
import domain.use_cases.RewriteUseCase
import echoError
import interface_adapters.cli.CommonOptions
import interface_adapters.outputtransformer.ErrorToStringTransformer
import interface_adapters.outputtransformer.SolutionToStringTransformer
import workingDir
import kotlin.io.path.*

class Rewrite : CliktCommand() {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get RDF surface from <path>").path().default(Path("-"))
    private val output by option("--output", "-o", help = "Write output to <path>").path().default(Path("-"))

    override fun help(context: Context) = "Parses and returns an RDF surface using a sublanguage of Notation 3"

    override fun run() {

        try {
            val (inputStream, baseIRI) = when {
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
            val rdfSurface = inputStream.bufferedReader().use { it.readText() }
            val result = RewriteUseCase(
                rdfSurface = rdfSurface,
                rdfList = commonOptions.rdfList,
                baseIRI = baseIRI,
                output = output
            )

            result.fold(
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