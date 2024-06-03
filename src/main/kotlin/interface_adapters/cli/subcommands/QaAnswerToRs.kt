package interface_adapters.cli.subcommands

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import domain.entities.rdf_term.IRI
import domain.error.fold
import domain.use_cases.CascQaAnswerToRsUseCase
import domain.use_cases.RawQaAnswerToRsUseCase
import echoError
import interface_adapters.cli.CommonOptions
import interface_adapters.outputtransformer.ErrorToStringTransformer
import interface_adapters.outputtransformer.SolutionToStringTransformer
import workingDir
import kotlin.io.path.*

class QaAnswerToRs :
    CliktCommand(help = "Transforms a TPTP tuple answer (--input) into an RDF surface using a specified RDF query or question surface (--q-surface)") {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get TPTP answer tuple from <path>").path().default(Path("-"))
    private val output by option("--output", "-o", help = "Write output to <path>").path(mustExist = false)
        .default(Path("-"))

    private val querySurface by option(
        "--q-surface",
        "-q",
        help = "Get RDF query or question surface from <path>"
    ).path(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).required()


    private val inputType by option("--input-type", "-it").choice("raw", "szs").default("szs")

    override fun run() {

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

            val rdfSurfaces = querySurface.readText()

            val result = if (inputType == "raw") {
                RawQaAnswerToRsUseCase.invoke(
                    inputStream = inputStream,
                    rdfSurface = rdfSurfaces,
                    baseIri = baseIri,
                    outputPath = output,
                    rdfList = commonOptions.rdfList
                )
            } else {
                CascQaAnswerToRsUseCase.invoke(
                    inputStream = inputStream,
                    rdfSurface = rdfSurfaces,
                    baseIri = baseIri,
                    outputPath = output,
                    rdfList = commonOptions.rdfList
                )
            }

            result.fold(
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