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

            val result = RDFSurfaceToFOLController().transformRDFSurfaceGraphToNotation3(
                rdfSurfaceGraph, commonOptions.rdfList
            )

            when (result) {
                is Success -> output?.writeText(result.value) ?: echo(result.value)
                is Failure -> {
                    echo(
                        "Failed to parse ${input.name}: " + result.failureMessage,
                        err = true,
                        trailingNewline = true
                    )
                    throw ProgramResult(1)
                }
            }
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.verbose) echo(exception.stackTraceToString(), err = true) else echo(
                exception.toString(),
                err = true
            )
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

            val result = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                rdfSurfaceGraph,
                ignoreQuerySurface,
                rdfLists = commonOptions.rdfList
            )

            when (result) {
                is Success -> output?.writeText(result.value) ?: echo(result.value)
                is Failure -> {
                    echo(
                        "Failed to parse ${input.name}: " + result.failureMessage,
                        err = true,
                        trailingNewline = true
                    )
                    throw ProgramResult(1)
                }
            }

        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.verbose) echo(exception.stackTraceToString(), err = true) else echo(
                exception.toString(),
                err = true
            )
        }
    }
}

class TPTPTupleAnswerToRDFSurfaces :
    CliktCommand(help = "Transform TPTP tuple answer to RDF Surfaces graph in respect to a RDFQuerySurface") {
    private val commonOptions by CommonOptions()

    private val output by option("--output", "-o", help = "write output to PATH").file(mustExist = false)

    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)

    private val querySurface by option("--querySurface", "-s", help = "path to RDF Query surface").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).prompt()

    private val tptpTupleAnswer by option("--tupleAnswer", "-t", help = "path to RDF Query surface").required()

    private val fOLAnswerTupleToRDFSurfaceController = FOLAnswerTupleToRDFSurfaceController()

    override fun run() {

        try {
            val rdfSurfaceGraph = querySurface.readText()

            val result = fOLAnswerTupleToRDFSurfaceController.transformTPTPTupleAnswerToRDFSurfaces(
                tptpTupleAnswer = tptpTupleAnswer,
                querySurface = rdfSurfaceGraph,
                quiet = quiet,
                verbose = commonOptions.verbose,
                rdfLists = commonOptions.rdfList
            )

            when (result) {
                is Success -> output?.writeText(result.value) ?: echo(result.value)
                is Failure -> {
                    echo(
                        "Failed to transform ${tptpTupleAnswer}: " + result.failureMessage,
                        err = true,
                        trailingNewline = true
                    )
                    throw ProgramResult(1)
                }
            }

        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.verbose) echo(exception.stackTraceToString(), err = true) else echo(
                exception.toString(),
                err = true
            )
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

            if (parseResult is Failure || answerParseResult is Failure) throw ProgramResult(1)

            if (!quiet) echo("Transformation was successful!")
            val file = output ?: File("transformationResults/" + input.nameWithoutExtension + ".p")

            file.parentFile.mkdirs()
            file.createNewFile()

            file.writeText("$parseResult\n$answerParseResult")
            val absolutePath = file.absolutePath

            if (!quiet) {
                echo("Problem output file: " + file.path)
                echo("Starting Vampire...")
            }

            val vampirePrompt =
                if (vampireOption == 0) "$vampireExec $absolutePath --output_mode smtcomp" else {
                    "$vampireExec -sa discount -awr 2 -s 1 -add large -afr on -afp 1000 -afq 2.0 -anc none -gsp on -lcm predicate -nm 64 -newcnf on -nwc 5 -sac on -urr ec_only -updr off $absolutePath --output_mode smtcomp"
                }


            val vampireProcess = vampirePrompt.runCommand(File(System.getProperty("user.dir")))

            vampireProcess?.waitFor(60, TimeUnit.SECONDS)

            val vampireResultString = vampireProcess?.inputStream?.reader()?.readText()?.lines()

            if (vampireResultString == null) {
                echo("Vampire Execution Error", err = true)
                throw ProgramResult(1)
            }

            //TODO()
            if (quiet) echo(vampireResultString.lastOrNull { it == "sat" || it == "unsat" } ?: "timeout") else
                vampireResultString.drop(1).forEach { if (!it.startsWith("WARNING") && it.isNotEmpty()) echo(it) }

        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.verbose) echo(exception.stackTraceToString(), err = true) else echo(
                exception.toString(),
                err = true
            )
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
        .default(File ("/home/rebekka/Programs/Vampire-qa/tmp/build/bin/vampire_z3_rel_qa_6176"))
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
                is Success -> {
                    if (!quiet) echo("Transformation was successful!")
                    if (parseResult.querySurfaces.isNullOrEmpty()) {
                        echo(
                            "Failed to transform ${input.name}: " + "Missing query surface in RDF surfaces graph",
                            err = true,
                            trailingNewline = true
                        )
                        throw ProgramResult(1)
                    }
                }

                is Failure -> {
                    echo(
                        "Failed to transform ${input.name}: " + parseResult.failureMessage,
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

            val vampirePrompt =
                if (vampireOption == 0) "$vampireExecFile -av off$cascCommand -qa answer_literal $absolutePath" else {
                    "$vampireExecFile -av off$cascCommand -sa discount -s 1 -add large -afp 4000 -afq 1.0 -anc none -gs on -gsem off -inw on -lcm reverse -lwlo on -nm 64 -nwc 1 -sas z3 -sos all -sac on -thi all -uwa all -updr off -uhcvi on -to lpo -qa answer_literal $absolutePath -t 2m"
                }

            val vampireProcess = vampirePrompt.runCommand(File(System.getProperty("user.dir")))

            vampireProcess?.waitFor(60, TimeUnit.SECONDS)

            //TODO(Generalization: https://www.tptp.org/TPTP/Proposals/AnswerExtraction.html)
            val vampireParsingResult = vampireProcess?.inputStream?.reader()?.useLines { vampireOutput ->
                FOLAnswerTupleToRDFSurfaceController().questionAnsweringOutputToRDFSurfacesCasc(
                    querySurface = parseResult.querySurfaces.first(),
                    verbose = commonOptions.verbose,
                    quiet = quiet,
                    questionAnsweringOutputLines = vampireOutput
                )
            }
            if (vampireParsingResult == null) {
                echo("Vampire Execution Error", err = true)
                throw ProgramResult(1)
            }
            echo(vampireParsingResult)
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.verbose) echo(exception.stackTraceToString(), err = true) else echo(
                exception.toString(),
                err = true
            )
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
    RdfSurfaceToFol().subcommands(Transform(), Check(), TransformWithQA(), Rewrite(), TPTPTupleAnswerToRDFSurfaces())
        .main(args)