import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import parser.VampireQuestionAnsweringResultsParser
import rdfSurfaces.RdfTripleElement
import java.io.File
import java.util.concurrent.TimeUnit

class RdfSurfaceToFol : CliktCommand() {
    override fun run() = Unit
}

class CommonOptions : OptionGroup("Standard Options") {
    val verbose by option("--verbose", "-v", help = "Display exception details").flag(default = false)
    val rdfList by option(
        "--rdf-list",
        "-r",
        help = "Use rdf:first-rdf:rest chain triples to translate RDF lists.\nIf set to \"false\", lists will be translated using FOL functions."
    ).flag(default = false)
}

class Rewrite : CliktCommand(help = "Parse and print RDF Surfaces graph using a Notation 3 sublanguage") {
    private val commonOptions by CommonOptions()
    private val output by option("--output", "-o", help = "write output to PATH").file(mustExist = false)
    private val input by option("--input", "-i", help = "file to RDF Surfaces graph").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).prompt()

    override fun run() {
        try {
            val rdfSurfaceGraph = input.readText()

            val (parseError, parseResultValue) = RDFSurfaceToFOLController().transformRDFSurfaceGraphToNotation3(
                rdfSurfaceGraph, commonOptions.rdfList
            )
            if (parseError) {
                echo(
                    "Failed to parse ${input.name}" + if (commonOptions.verbose) ":\n$parseResultValue" else "",
                    err = true,
                    trailingNewline = true
                )
                throw ProgramResult(1)
            }
            output?.writeText(parseResultValue) ?: println(parseResultValue)
        } catch (exception: CliktError) {
            throw exception
        } catch (exception: ProgramResult) {
            throw exception
        } catch (exception: Exception) {
            echo(exception.toString(), err = true)
            if (commonOptions.verbose) echo(exception.stackTraceToString(), err = true)
        }
    }
}


class Transform : CliktCommand(help = "Transform RDF Surfaces graph to FOL formula in TPTP format") {
    private val commonOptions by CommonOptions()

    private val ignoreQuerySurface by option("--ignoreQuerySurface").flag(default = false)
    private val output by option("--output", "-o", help = "write output to PATH").file(mustExist = false)

    private val input by option("--input", "-i", help = "file to RDF Surfaces graph").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).prompt()

    override fun run() {

        try {
            val rdfSurfaceGraph = input.readText()

            val (parseError, parseResultValue) = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                rdfSurfaceGraph,
                ignoreQuerySurface,
                rdfLists = commonOptions.rdfList
            )
            if (parseError) {
                echo(
                    "Failed to parse ${input.name}" + if (commonOptions.verbose) ":\n$parseResultValue" else "",
                    err = true,
                    trailingNewline = true
                )
                throw ProgramResult(1)
            }
            output?.writeText(parseResultValue) ?: println(parseResultValue)

        } catch (exception: CliktError) {
            throw exception
        } catch (exception: ProgramResult) {
            throw exception
        } catch (exception: Exception) {
            echo(exception.toString(), err = true)
            if (commonOptions.verbose) echo(exception.stackTraceToString(), err = true)
        }
    }
}

class Check :
    CliktCommand(help = "Check if the consequences of an RDF Surfaces reasoner are also logical consequences of the FOL-based Vampire Theorem Proofer") {
    private val commonOptions by CommonOptions()

    private val input by option(
        "--input", "-i",
        help = "Path of the file with the RDF Surface graph"
    ).file().prompt()
    private val expectedAnswer by option(
        "--expected-answer", "-ea",
        help = "Path of the file with the expected solution"
    ).file()
    private val vampireExec by option(help = "file to Vampire executable").file()
        .default(File("/home/rebekka/Programs/Vampire-qa/tmp/build/bin/vampire_z3_rel_qa_6176"))
    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)
    private val output by option(
        "--output",
        "-o",
        help = "write transformation output to PATH"
    ).file(mustExist = false)


    override fun run() {

        try {
            val rdfSurfaceToFOLController = RDFSurfaceToFOLController()

            val computedAnswerFile =
                expectedAnswer ?: File(input.parentFile.path + "/" + input.nameWithoutExtension + "-answer.n3s")

            val graph = input.readText()
            val answerGraph = computedAnswerFile.readText()


            val (parseError, parseResultValue) = rdfSurfaceToFOLController.transformRDFSurfaceGraphToFOL(
                graph,
                ignoreQuerySurface = true,
                rdfLists = commonOptions.rdfList
            )
            val (answerParseError, answerParseResultValue) = rdfSurfaceToFOLController.transformRDFSurfaceGraphToFOLConjecture(
                answerGraph,
                rdfLists = commonOptions.rdfList
            )
            if (parseError or answerParseError) {
                echo("RDF Surfaces Parser Error", err = true, trailingNewline = true)
                if (!quiet) {
                    if (parseError) echo(
                        "Failed to parse ${input.name}" + if (commonOptions.verbose) ":\n$parseResultValue" else "",
                        err = true,
                        trailingNewline = true
                    )
                    if (answerParseError) echo(
                        "Failed to parse ${computedAnswerFile.name}" + if (commonOptions.verbose) ":\n$answerParseResultValue" else "",
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
                echo("Vampire Execution Error", err = true)
                throw ProgramResult(1)
            }

            if (quiet) println(
                vampireResultString.drop(1).dropLastWhile
                { it.isBlank() }.joinToString(separator = " --- ")
            ) else vampireResultString.drop(1).forEach { println(it) }

        } catch (exception: CliktError) {
            throw exception
        } catch (exception: ProgramResult) {
            throw exception
        } catch (exception: Exception) {
            echo(exception.toString(), err = true)
            if (commonOptions.verbose) echo(exception.stackTraceToString(), err = true)
        }
    }
}

class CheckWithQuestionAnswering :
    CliktCommand(help = "Transform an RDF Surfaces graph to FOL and show the results of the Vampire question answering feature") {
    private val commonOptions by CommonOptions()

    val input by option(
        "--input", "-i",
        help = "file to RDF Surfaces graph"
    ).file().prompt()
    private val vampireExecFile by option(help = "file to vampire executable").file()
        .default(File("/home/rebekka/Programs/Vampire-qa/tmp/build/bin/vampire_z3_rel_qa_6176"))
    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)

    private val casc by option("--casc", "-c").flag(default = false)

    private val output by option(
        "--output",
        "-o",
        help = "write transformation output to PATH"
    ).file(mustExist = false)

    private val vampireOption by option("--vampire-option").choice("0", "1").int().default(0)

    override fun run() {
        try {
            val cascCommand = if (casc) " --mode casc" else ""

            val graph = input.readText()

            val (parseError, parseResultValue, querySurfaces) = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                graph,
                false,
                rdfLists = commonOptions.rdfList
            )

            if (parseError) {
                echo("Transforming Error", err = true, trailingNewline = true)
                if (commonOptions.verbose) echo(
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
                if (vampireOption == 0) {
                    "$vampireExecFile -av off$cascCommand -qa answer_literal $absolutePath".runCommand(
                        File(
                            System.getProperty(
                                "user.dir"
                            )
                        )
                    )
                } else {
                    "$vampireExecFile -av off$cascCommand -sa discount -s 1 -add large -afp 4000 -afq 1.0 -anc none -gs on -gsem off -inw on -lcm reverse -lwlo on -nm 64 -nwc 1 -sas z3 -sos all -sac on -thi all -uwa all -updr off -uhcvi on -to lpo -qa answer_literal $absolutePath -t 2m".runCommand(
                        File(
                            System.getProperty(
                                "user.dir"
                            )
                        )
                    )
                }

            vampireProcess?.waitFor(30, TimeUnit.SECONDS)

            val vampireParsingResult = vampireProcess?.inputStream?.reader()?.useLines { vampireOutput ->
                val result = mutableSetOf<List<RdfTripleElement>>()
                val orResult = mutableSetOf<List<List<RdfTripleElement>>>()
                vampireOutput.forEach {
                    if (it.startsWith("% SZS answers Tuple")) {
                        val rawVampireOutputLine =
                            it.trimStart { char -> (char != '[') }.trimEnd { char -> char != ']' }
                        when (val parserResult =
                            VampireQuestionAnsweringResultsParser.tryParseToEnd(rawVampireOutputLine)) {
                            is Parsed -> {
                                result.addAll(parserResult.value.first)
                                orResult.addAll(parserResult.value.second)
                            }

                            is ErrorResult -> println("Error: $it + $parserResult")
                        }
                    }
                }

                if (result.isEmpty()) return@useLines "No solutions"
                return@useLines querySurfaces?.let {
                    RDFSurfaceToFOLController().transformQuestionAnsweringResult(
                        result,
                        it.first()
                    )
                } ?: result.joinToString(prefix = "- ")
            }
            println(vampireParsingResult)
        } catch (exception: CliktError) {
            throw exception
        } catch (exception: ProgramResult) {
            throw exception
        } catch (exception: Exception) {
            echo(exception.toString(), err = true)
            if (commonOptions.verbose) echo(exception.stackTraceToString(), err = true)
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
    RdfSurfaceToFol().subcommands(Transform(), Check(), CheckWithQuestionAnswering(), Rewrite())
        .main(args)