import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import parser.VampireQuestionAnsweringResultsParser
import java.io.File
import java.util.concurrent.TimeUnit

class RdfSurfaceToFol : CliktCommand() {
    override fun run() = Unit
}

class Rewrite : CliktCommand(help = "Parse and print RDF Surfaces graph using a Notation 3 sublanguage") {
    private val output by option("--output", "-o", help = "Write output to PATH").file(mustExist = false)
    private val input by option("--input", "-i", help = "Path to the RDF Surface graph").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).prompt()
    private val verbose by option("--verbose", "-v", help = "Display exception details").flag(default = false)
    private val rdfList by option("--rdf-list", "-r", help = "Use first-rest chains").flag(default = false)


    override fun run() {
        try {
            val rdfSurfaceGraph = input.readText()

            val (parseError, parseResultValue) = RDFSurfaceToFOLController().transformRDFSurfaceGraphToNotation3(
                rdfSurfaceGraph, rdfList
            )
            if (parseError) {
                echo(
                    "Failed to parse ${input.name}" + if (verbose) ":\n$parseResultValue" else "",
                    err = true,
                    trailingNewline = true
                )
                throw ProgramResult(1)
            }
            output?.writeText(parseResultValue) ?: println(parseResultValue)
        } catch (exception: ProgramResult) {
            throw exception
        } catch (exception: Exception) {
            echo(exception.toString(), err = true)
            if (verbose) echo(exception.stackTraceToString(), err = true)
        }
    }
}


class Transform : CliktCommand(help = "Transform RDF Surfaces graph to FOL formula in TPTP format") {
    private val ignoreQuerySurface by option("--ignoreQuerySurface").flag(default = false)
    private val output by option("--output", "-o", help = "Write output to PATH").file(mustExist = false)

    private val input by option("--input", "-i", help = "Path to the RDF Surface graph").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).prompt()
    private val verbose by option("--verbose", "-v", help = "Display exception details").flag(default = false)

    private val rdfList by option("--rdf-list", "-r", help = "Use first-rest chains").flag(default = false)


    override fun run() {

        try {
            val rdfSurfaceGraph = input.readText()

            val (parseError, parseResultValue) = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                rdfSurfaceGraph,
                ignoreQuerySurface,
                rdfLists = rdfList
            )
            if (parseError) {
                echo(
                    "Failed to parse ${input.name}" + if (verbose) ":\n$parseResultValue" else "",
                    err = true,
                    trailingNewline = true
                )
                throw ProgramResult(1)
            }
            output?.writeText(parseResultValue) ?: println(parseResultValue)

        } catch (exception: ProgramResult) {
            throw exception
        } catch (exception: Exception) {
            echo(exception.toString(), err = true)
            if (verbose) echo(exception.stackTraceToString(), err = true)
        }
    }
}

class Check : CliktCommand() {
    private val input by option(
        "--input", "-i",
        help = "Path of the file with the RDF Surface graph"
    ).file().prompt()
    private val expectedAnswer by option(
        "--expected-answer", "-ea",
        help = "Path of the file with the expected solution"
    ).file()
    private val vampireExec by option(help = "Vampire execution file").file()
        .default(File("/home/rebekka/Programs/Vampire-qa/tmp/build/bin/vampire_z3_rel_qa_6176"))
    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)
    private val verbose by option("--verbose", "-v", help = "Display exception details").flag(default = false)

    private val rdfSurfaceToFOLController = RDFSurfaceToFOLController()

    private val output by option(
        "--output",
        "-o",
        help = "write transformation output to PATH"
    ).file(mustExist = false)

    private val rdfList by option("--rdf-list", "-r", help = "Use first-rest chains").flag(default = false)


    override fun run() {

        try {
            val computedAnswerFile =
                expectedAnswer ?: File(input.parentFile.path + "/" + input.nameWithoutExtension + "-answer.n3s")

            val graph = input.readText()
            val answerGraph = computedAnswerFile.readText()


            val (parseError, parseResultValue) = rdfSurfaceToFOLController.transformRDFSurfaceGraphToFOL(
                graph,
                ignoreQuerySurface = true,
                rdfLists = rdfList
            )
            val (answerParseError, answerParseResultValue) = rdfSurfaceToFOLController.transformRDFSurfaceGraphToFOLConjecture(
                answerGraph,
                rdfLists = rdfList
            )
            if (parseError or answerParseError) {
                echo("RDF Surfaces Parser Error", err = true, trailingNewline = true)
                if (!quiet) {
                    if (parseError) echo(
                        "Failed to parse ${input.name}:\n" + if (verbose) parseResultValue else "",
                        err = true,
                        trailingNewline = true
                    )
                    if (answerParseError) echo(
                        "Failed to parse ${computedAnswerFile.name}:\n" + if (verbose) answerParseResultValue else "",
                        err = true,
                        trailingNewline = true
                    )
                }
                throw ProgramResult(1)
            }
            if (!quiet) println("Transformation was successful!")
            val file = output ?: File("transformationResults/" + input.nameWithoutExtension + ".p")

            file.parentFile.mkdirs()
            file.createNewFile()

            file.writeText("$parseResultValue\n$answerParseResultValue")
            val absolutePath = file.absolutePath

            if (!quiet) {
                println("Problem output file: " + file.path)
                println("Starting Vampire...")
            }

            val vampireProcess = "$vampireExec --output_mode smtcomp $absolutePath".runCommand(
                File(
                    System.getProperty(
                        "user.dir"
                    )
                )
            )

            vampireProcess?.waitFor()

            val vampireResultString = vampireProcess?.inputStream?.reader()?.readText()?.lines()

            if (vampireResultString == null) {
                echo("Vampire Error", err = true)
                throw ProgramResult(1)
            }

            if (!quiet) vampireResultString.drop(1).forEach { println(it) }

            if (quiet) println(
                vampireResultString.drop(1).dropLastWhile
                { it.isBlank() }.joinToString(separator = " --- ")
            )
        } catch (exception: ProgramResult) {
            throw exception
        } catch (exception: Exception) {
            echo(exception.toString(), err = true)
            if (verbose) echo(exception.stackTraceToString(), err = true)
        }
    }
}

class CheckWithVampireQuestionAnswering : CliktCommand() {
    val input by option(
        "--input", "-i",
        help = "RDF Surfaces graph file"
    ).file().prompt()
    private val vampireExecFile by option(help = "Path of the vampire execution file").file()
        .default(File("/home/rebekka/Programs/Vampire-qa/tmp/build/bin/vampire_z3_rel_qa_6176"))
    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)

    private val casc by option("--casc", "-c").flag(default = false)

    private val output by option(
        "--output",
        "-o",
        help = "write transformation output to PATH"
    ).file(mustExist = false)
    private val verbose by option("--verbose", "-v", help = "Display exception details").flag(default = false)

    private val rdfList by option("--rdf-list", "-r", help = "Use first-rest chains").flag(default = false)

    override fun run() {
        try {
            val cascCommand = if (casc) " --mode casc" else ""

            val graph = input.readText()

            val (parseError, parseResultValue) = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                graph,
                false,
                rdfLists = rdfList
            )

            if (parseError) {
                echo("Transforming Error", err = true, trailingNewline = true)
                if (verbose) echo(
                    "Failed to parse " + input.name + ":\n" + parseResultValue,
                    err = true,
                    trailingNewline = true
                )
                throw ProgramResult(1)
            }

            if (!quiet) println("Transformation was successful!")

            val file = output ?: File("transformationResults/" + input.nameWithoutExtension + ".p")

            file.parentFile.mkdirs()
            file.createNewFile()

            file.writeText(parseResultValue)
            val absolutePath = file.absolutePath

            if (!quiet) {
                println("Problem output file: " + file.path)
                println("Starting Vampire...")
            }

            val vampireProcess =
//                "$vampireExecFile -av off$cascCommand -sa discount -s 1 -add large -afp 4000 -afq 1.0 -anc none -gs on -gsem off -inw on -lcm reverse -lwlo on -nm 64 -nwc 1 -sas z3 -sos all -sac on -thi all -uwa all -updr off -uhcvi on -to lpo -qa answer_literal $absolutePath -t 2m".runCommand(
//                    File(
//                        System.getProperty(
//                            "user.dir"
//                        )
//                    )
//                )
                "$vampireExecFile -av off$cascCommand -qa answer_literal $absolutePath".runCommand(
                    File(
                        System.getProperty(
                            "user.dir"
                        )
                    )
                )

            vampireProcess?.waitFor(30, TimeUnit.SECONDS)

            val vampireParsingResult = vampireProcess?.inputStream?.reader()?.useLines { vampireOutput ->
                val result = mutableSetOf<String>()
                val orResult = mutableSetOf<String>()
                vampireOutput.forEach { vampireOutputLine ->
                    if (vampireOutputLine.startsWith("% SZS answers Tuple")) {
                        val rawVampireOutputLine =
                            vampireOutputLine.trimStart { char -> (char != '[') }.trimEnd { char -> char != ']' }
                        when (val parserResult =
                            VampireQuestionAnsweringResultsParser.tryParseToEnd(rawVampireOutputLine)) {
                            is Parsed -> {
                                result.addAll(parserResult.value.first)
                                orResult.addAll(parserResult.value.second)
                            }

                            is ErrorResult -> println("Error: $vampireOutputLine")
                        }
                    }
                }
                if (result.isEmpty()) return@useLines "No solutions"
                result.joinToString(
                    separator = "\n- ",
                    prefix = "\n- "
                )
            }
            println(vampireParsingResult)
        } catch (exception: ProgramResult) {
            throw exception
        } catch (exception: Exception) {
            echo(exception.toString(), err = true)
            if (verbose) echo(exception.stackTraceToString(), err = true)
        }
    }
}

fun String.runCommand(workingDir: File): Process? {
    return ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectErrorStream(true)
        .start()
}

fun main(args: Array<String>) =
    RdfSurfaceToFol().subcommands(Transform(), Check(), CheckWithVampireQuestionAnswering(), Rewrite())
        .main(args)