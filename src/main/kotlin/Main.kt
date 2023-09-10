import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import parser.TPTPTupleAnswerParser
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
    private val input by option("--input", "-i", help = "path to RDF Surfaces graph").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).prompt()

    override fun run() {
        try {
            val rdfSurfaceGraph = input.readText()

            val parseResult = RDFSurfaceToFOLController().transformRDFSurfaceGraphToNotation3(
                rdfSurfaceGraph, commonOptions.rdfList
            )

            when (parseResult) {
                is Success -> output?.writeText(parseResult.value) ?: echo(parseResult.value)
                is Failure -> {
                    echo(
                        "Failed to parse ${input.name}: " + parseResult.failureMessage,
                        err = true,
                        trailingNewline = true
                    )
                    throw ProgramResult(1)
                }
            }
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

    private val input by option("--input", "-i", help = "path to RDF Surfaces graph").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).prompt()

    override fun run() {

        try {
            val rdfSurfaceGraph = input.readText()

            val parseResult = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                rdfSurfaceGraph,
                ignoreQuerySurface,
                rdfLists = commonOptions.rdfList
            )

            when (parseResult) {
                is Success -> output?.writeText(parseResult.value) ?: echo(parseResult.value)
                is Failure -> {
                    echo(
                        "Failed to parse ${input.name}: " + parseResult.failureMessage,
                        err = true,
                        trailingNewline = true
                    )
                    throw ProgramResult(1)
                }
            }

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
        help = "path to RDF Surfaces graph"
    ).file().prompt()
    private val expectedAnswer by option(
        "--expected-answer", "-ea",
        help = "Path to the expected solution"
    ).file()
    private val vampireExec by option(help = "path to Vampire executable").file()
        .default(File("/home/rebekka/Programs/Vampire-qa/tmp/build/bin/vampire_z3_rel_qa_6176"))
    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)
    private val output by option(
        "--output",
        "-o",
        help = "write transformation output to PATH"
    ).file(mustExist = false)

    private val casc by option("--casc", "-c").flag(default = false)

    private val vampireOption by option("--vampire-option").choice("0", "1").int().default(0)


    override fun run() {

        try {
            val rdfSurfaceToFOLController = RDFSurfaceToFOLController()

            //TODO()
            val cascCommand = if (casc) " --mode casc" else ""

            val computedAnswerFile =
                expectedAnswer ?: File(input.parentFile.path + "/" + input.nameWithoutExtension + "-answer.n3s")

            val graph = input.readText()
            val answerGraph = computedAnswerFile.readText()



            val parseResult = rdfSurfaceToFOLController.transformRDFSurfaceGraphToFOL(
                graph,
                ignoreQuerySurface = true,
                rdfLists = commonOptions.rdfList
            )
            val answerParseResult = rdfSurfaceToFOLController.transformRDFSurfaceGraphToFOLConjecture(
                answerGraph,
                rdfLists = commonOptions.rdfList
            )

            when (parseResult) {
                is Success -> output?.writeText(parseResult.value) ?: echo(parseResult.value)
                is Failure -> {
                    echo(
                        "Failed to parse ${input.name}: " + parseResult.failureMessage,
                        err = true,
                        trailingNewline = true
                    )
                }
            }

            when (answerParseResult) {
                is Success -> output?.writeText(answerParseResult.value) ?: echo(answerParseResult.value)
                is Failure -> {
                    echo(
                        "Failed to parse ${input.name}: " + answerParseResult.failureMessage,
                        err = true,
                        trailingNewline = true
                    )
                }
            }

            if (parseResult is Failure || answerParseResult is Failure ) throw ProgramResult(1)

            if (!quiet) echo("Transformation was successful!")
            val file = output ?: File("transformationResults/" + input.nameWithoutExtension + ".p")

            file.parentFile.mkdirs()
            file.createNewFile()

            file.writeText("$parseResult\n$answerParseResult")
            val absolutePath = file.absolutePath

            if (!quiet) {
                println("Problem output file: " + file.path)
                println("Starting Vampire...")
            }

            val vampireProcess =
                if (vampireOption == 0) {
                    "$vampireExec $absolutePath --output_mode smtcomp".runCommand(
                        File(
                            System.getProperty(
                                "user.dir"
                            )
                        )
                    )
                } else {
                    "$vampireExec -sa discount -awr 2 -s 1 -add large -afr on -afp 1000 -afq 2.0 -anc none -gsp on -lcm predicate -nm 64 -newcnf on -nwc 5 -sac on -urr ec_only -updr off $absolutePath --output_mode smtcomp".runCommand(
                        File(
                            System.getProperty(
                                "user.dir"
                            )
                        )
                    )
                }

            vampireProcess?.waitFor(60, TimeUnit.SECONDS)

            val vampireResultString = vampireProcess?.inputStream?.reader()?.readText()?.lines()

            if (vampireResultString == null) {
                echo("Vampire Execution Error", err = true)
                throw ProgramResult(1)
            }

            //TODO()
            if (quiet) println(vampireResultString.lastOrNull { it == "sat" || it == "unsat" } ?: "timeout") else
                vampireResultString.drop(1).forEach { if (!it.startsWith("WARNING") && it.isNotEmpty()) println(it) }

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

class TransformWithQA :
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

            val parseResult = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                graph,
                false,
                rdfLists = commonOptions.rdfList
            )

            when (parseResult) {
                is Success -> {if (!quiet) println("Transformation was successful!")}
                is Failure -> {
                    echo(
                        "Failed to parse ${input.name}: " + parseResult.failureMessage,
                        err = true,
                        trailingNewline = true
                    )
                    throw ProgramResult(1)
                }
            }


            val file = output ?: File("transformationResults/" + input.nameWithoutExtension + ".p")

            file.parentFile.mkdirs()
            file.createNewFile()

            file.writeText(parseResult.value)
            val absolutePath = file.absolutePath

            if (!quiet) {
                echo("Problem output file: " + file.path)
                echo("Starting Vampire...")
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

            vampireProcess?.waitFor(60, TimeUnit.SECONDS)

            //TODO(Generalization: https://www.tptp.org/TPTP/Proposals/AnswerExtraction.html)
            val vampireParsingResult = vampireProcess?.inputStream?.reader()?.useLines { vampireOutput ->
                val parsedResult = mutableSetOf<List<RdfTripleElement>>()
                val rawResult = mutableSetOf<String>()
                val orResult = mutableSetOf<List<List<RdfTripleElement>>>()
                vampireOutput.forEach {
                    if (it.contains("SZS answers Tuple")) {
                        val rawVampireOutputLine =
                            it.trimStart { char -> (char != '[') }.trimEnd { char -> char != ']' }
                        when (val parserResult =
                            TPTPTupleAnswerParser.tryParseToEnd(rawVampireOutputLine)) {
                            is Parsed -> {
                                parsedResult.addAll(parserResult.value.first)
                                orResult.addAll(parserResult.value.second)
                            }

                            is ErrorResult -> rawResult.add(rawVampireOutputLine)
                        }
                    }
                }

                if (parsedResult.isEmpty() && rawResult.isEmpty() && !quiet) return@useLines "No solutions"
                val answerRDFSurfacesGraph = if (parsedResult.isNotEmpty()) (parseResult.querySurfaces?.let {
                    RDFSurfaceToFOLController().transformQuestionAnsweringResult(
                        parsedResult,
                        it.first()
                    )
                } ?: parsedResult.joinToString(prefix = "- ")) else ""
                val notParsedAnswers = if (rawResult.isNotEmpty()) rawResult.joinToString(
                    prefix = "Not parsed results: \n",
                    separator = "\n"
                ) else ""
                return@useLines answerRDFSurfacesGraph + (if (notParsedAnswers.isNotEmpty() && answerRDFSurfacesGraph.isNotEmpty()) "\n" else "") + notParsedAnswers
            }
            if (vampireParsingResult == null) {
                echo("Vampire Execution Error", err = true)
                throw ProgramResult(1)
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
    RdfSurfaceToFol().subcommands(Transform(), Check(), TransformWithQA(), Rewrite())
        .main(args)