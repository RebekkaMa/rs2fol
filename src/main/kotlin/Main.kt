import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextStyle
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

val debugTextStyle = TextStyle(italic = true)
val errorKeyWordTextStyle = TextStyle(color = red)

class RdfSurfaceToFol : CliktCommand() {
    init {
        context {
            helpFormatter = { MordantHelpFormatter(it, showDefaultValues = true) }
        }
    }

    override fun run() = Unit
}

class CommonOptions : OptionGroup("Standard Options") {
    val debug by option("--debug", "-d", help = "Show debugging output").flag(default = false)
    val rdfList by option(
        "--rdf-list",
        "-r",
        help = "Use the rdf:first/rdf:rest list structure to translate RDF lists.\nIf set to \"false\", lists will be translated using FOL functions."
    ).flag(default = false)
}

class Rewrite : CliktCommand(help = "Parse and print RDF surfaces graph using a Notation 3 sublanguage") {
    private val commonOptions by CommonOptions()
    private val output by option("--output", "-o", help = "Write output to <path>").path()
    private val input by option("--input", "-i", help = "Get RDF surfaces graph from <path>").inputStream().defaultStdin()

    override fun run() {
        try {
            input.bufferedReader().use {
                val result = RDFSurfaceToFOLController().transformRDFSurfaceGraphToNotation3(
                    it.readText(), commonOptions.rdfList
                )
                when (result) {
                    is Success -> output?.writeText(result.value) ?: echo(result.value)
                    is Failure -> {
                        echoError("Failed to rewrite the RDF surfaces graph. " + result.failureMessage)
                        throw ProgramResult(1)
                    }
                }

            }
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echo(exception.stackTraceToString(), err = true) else echo(
                exception.toString(),
                err = true
            )
        }
    }
}

class Transform : CliktCommand(help = "Transform RDF surfaces graph to FOL formula in TPTP format") {
    private val commonOptions by CommonOptions()

    private val ignoreQuerySurface by option("--ignoreQuerySurface").flag(default = false)
    private val output by option("--output", "-o", help = "Write output to <path>").path(mustExist = false)

    private val input by option("--input", "-i", help = "Path to RDF surfaces graph").inputStream().defaultStdin()

    override fun run() {

        try {
            val rdfSurfacesGraph = input.reader().use { it.readText() }
            val result = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                rdfSurfacesGraph,
                ignoreQuerySurface,
                rdfLists = commonOptions.rdfList
            )
            when (result) {
                is Success -> output?.writeText(result.value) ?: echo(result.value)
                is Failure -> {
                    echoError("Failed to transform the RDF surfaces graph: " + result.failureMessage)
                    throw ProgramResult(1)
                }
            }
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}

class TPTPTupleAnswerToRDFSurfaces :
    CliktCommand(help = "Transform TPTP tuple answer to RDF surfaces graph in respect to a RDF query surface") {
    private val commonOptions by CommonOptions()

    private val output by option("--output", "-o", help = "Write output to <path>").path(mustExist = false)

    private val querySurface by option("--querySurface", "-s", help = "Get RDF query surface from <path>").path(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).required()

    private val tptpTupleAnswer by option("--tuple-answer", "-t", help = "Path to RDF query surface").inputStream()
        .defaultStdin()

    private val fOLAnswerTupleToRDFSurfaceController = FOLAnswerTupleToRDFSurfaceController()

    private val inputType by option("--input-option", "-io").choice("raw", "szs").default("szs")


    override fun run() {

        try {
            val rdfSurfaceGraph = querySurface.readText()

            val result =  if (inputType == "raw") {
                    fOLAnswerTupleToRDFSurfaceController.transformTPTPTupleAnswerToRDFSurfaces(
                        tptpTupleAnswer = tptpTupleAnswer.bufferedReader().use { it.readText() },
                        querySurfaceStr = rdfSurfaceGraph,
                        debug = commonOptions.debug,
                        rdfLists = commonOptions.rdfList,
                    )
                } else {
                    tptpTupleAnswer.bufferedReader().useLines {
                        fOLAnswerTupleToRDFSurfaceController.questionAnsweringOutputToRDFSurfacesCasc(
                            querySurface = rdfSurfaceGraph,
                            questionAnsweringOutputLines = it,
                            debug = commonOptions.debug,
                            rdfLists = commonOptions.rdfList
                        )
                    }
                }
            when (result) {
                is AnswerTupleToRDFSurfacesGraphSuccess -> output?.writeText(result.value) ?: echo(result.value)
                is AnswerTupleToRDFSurfacesGraphFailure -> {
                    echoError("Failed to transform the TPTP tuple answer: " + result.failureMessage)
                    throw ProgramResult(1)
                }
            }
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}


class Check :
    CliktCommand(help = "Check if the consequences of an RDF surfaces reasoner are also logical consequences of the FOL-based Vampire Theorem Proofer") {
    private val commonOptions by CommonOptions()

    private val input by option(
        "--input", "-i",
        help = "Path to RDF Surfaces graph"
    ).path(mustExist = true).prompt()

    private val expectedAnswer by option(
        "--expected-answer", "-ea",
        help = "Path to the expected solution"
    ).path().defaultLazy("<<file>-answer.n3s>") {
        Path(input.parent.pathString + "/" + input.nameWithoutExtension + "-answer.n3s").takeIf { it.exists() }
            ?: Path(input.parent.pathString + "/" + input.nameWithoutExtension + "-answer.n3").takeIf { it.exists() }
            ?: throw FileNotFound(input.parent.pathString + "/" + input.nameWithoutExtension + "-answer.n3s")
    }
    private val vampireExec by option(help = "Path to Vampire executable").path()
        .default(Path("/home/rebekka/Programs/Vampire-qa/tmp/build/bin/vampire_z3_rel_qa_6176"))
    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)
    private val output by option(
        "--output",
        "-o",
        help = "Write transformation output to <path>"
    ).path(mustExist = false)

    private val vampireOption by option("--vampire-option", "-v").choice("0", "1").int().default(0)

    private val timeLimit by option("--time-limit", "-t", help = "Reasoning time limit in seconds").long().default(60)
        .validate { it > 0 }


    override fun run() {

        try {
            val rdfSurfaceToFOLController = RDFSurfaceToFOLController()

            val graph = input.readText()
            val answerGraph = expectedAnswer.readText()

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
                is Success -> {
                    output?.writeText(parseResult.value)
                    if (quiet.not()) echoNonQuiet("Transformation of RDF surfaces graph was successful")
                }

                is Failure -> {
                    echoError("Failed to parse ${input.name}: " + parseResult.failureMessage)
                }
            }

            when (answerParseResult) {
                is Success -> {
                    output?.writeText(answerParseResult.value)
                    if (quiet.not() && parseResult is Success) echoNonQuiet("Transformation of the consequences RDF surfaces graph was successful")
                }

                is Failure -> {
                    echoError("Failed to parse ${expectedAnswer.name}" + answerParseResult.failureMessage)
                }
            }

            if (parseResult is Failure || answerParseResult is Failure) throw ProgramResult(1)

            val path = output ?: Path("transformationResults/" + input.nameWithoutExtension + ".p")

            if (path.parent.isDirectory().not()) path.parent.createDirectory()
            if (path.exists().not()) path.createFile()

            val parseResultSuccess = parseResult as Success
            val answerResultSuccess = answerParseResult as Success

            path.writeText(parseResultSuccess.value + System.lineSeparator() + answerResultSuccess.value)
            val absolutePath = path.absolutePathString()

            if (!quiet) {
                echoNonQuiet("Problem output file: $path")
                echoNonQuiet("Starting Vampire...")
            }

            val vampirePrompt =
                if (vampireOption == 0) "$vampireExec $absolutePath --output_mode smtcomp" else {
                    "$vampireExec -sa discount -awr 2 -s 1 -add large -afr on -afp 1000 -afq 2.0 -anc none -gsp on -lcm predicate -nm 64 -newcnf on -nwc 5 -sac on -urr ec_only -updr off $absolutePath --output_mode smtcomp"
                }

            val vampireProcess = vampirePrompt.runCommand(File(System.getProperty("user.dir")))

            val noTimeout = vampireProcess?.waitFor(timeLimit, TimeUnit.SECONDS)

            if (noTimeout?.not() == true) {
                echo("timeout")
                vampireProcess.destroy()
                if (vampireProcess.isAlive) vampireProcess.destroyForcibly()
                return
            }

            val vampireResultString = vampireProcess?.inputReader()?.readLines()

            if (vampireResultString == null) {
                echoError("Vampire Execution Error")
                throw ProgramResult(1)
            }

            vampireResultString.lastOrNull { it == "sat" || it == "unsat" }
                ?.let { echo(if (it.startsWith('s')) TextColors.rgb("#ff9933")(it) else TextColors.green(it)); return }
            vampireResultString.lastOrNull { it.contains("error", true) }?.let { echoError(it); return }

            echoError(
                "Unknown Vampire output" + if (commonOptions.debug) vampireResultString.joinToString(
                    prefix = "Vampire output:${System.lineSeparator()}",
                    separator = System.lineSeparator()
                ) else ""
            )

        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}

class TransformWithQA :
    CliktCommand(help = "Transform an RDF surfaces graph to FOL and show the results of the Vampire question answering feature") {
    private val commonOptions by CommonOptions()

    private val input by option(
        "--input", "-i",
        help = "File to RDF Surfaces graph"
    ).inputStream().defaultStdin()
    private val vampireExecFile by option(help = "File to vampire executable").path()
        .default(Path("/home/rebekka/Programs/Vampire-qa/tmp/build/bin/vampire_z3_rel_qa_6176"))
    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)

    private val casc by option("--casc", "-c").flag(default = false)

    private val output by option(
        "--output",
        "-o",
        help = "Write transformation output to <path>"
    ).path(mustExist = false)

    private val vampireOption by option("--vampire-option", "-v").choice("0", "1").int().default(0)

    private val timeLimit by option("--time-limit", "-t", help = "Reasoning time limit in seconds").long().default(60)
        .validate { it > 0 }


    override fun run() = runBlocking {
        try {
            val cascCommand = if (casc) " --mode casc" else ""


            val parseResult = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                input.reader().use { it.readText() },
                false,
                rdfLists = commonOptions.rdfList
            )
            when (parseResult) {
                is Success -> {
                    if (!quiet) echoNonQuiet("Transformation was successful!")
                    if (parseResult.querySurfaces.isNullOrEmpty()) {
                        echoError("Failed to transform RDF surfaces graph: " + "Missing query surface in RDF surfaces graph")
                        throw ProgramResult(1)
                    }
                    if (parseResult.querySurfaces.size > 1) echoError("There are too many query surfaces. Only one query surface is currently supported.")
                }

                is Failure -> {
                    echoError("Failed to transform RDF surfaces graph: " + parseResult.failureMessage)
                    throw ProgramResult(1)
                }
            }

            val path = output ?: Path("transformationResults/tptpProblem.p")

            if (path.parent.isDirectory().not()) path.parent.createDirectory()
            if (path.exists().not()) path.createFile()

            path.writeText(parseResult.value)
            val absolutePath = path.toAbsolutePath()

            if (!quiet) {
                echoNonQuiet("Problem output file: $path")
                echoNonQuiet("Starting Vampire...")
            }

            val vampirePrompt =
                if (vampireOption == 0) "$vampireExecFile -av off$cascCommand -qa answer_literal $absolutePath -om smtcomp" else {
                    "$vampireExecFile -av off$cascCommand -sa discount -s 1 -add large -afp 4000 -afq 1.0 -anc none -gs on -gsem off -inw on -lcm reverse -lwlo on -nm 64 -nwc 1 -sas z3 -sos all -sac on -thi all -uwa all -updr off -uhcvi on -to lpo -qa answer_literal $absolutePath -om smtcomp"
                }

            val vampireProcess = vampirePrompt.runCommand(File(System.getProperty("user.dir")))

            val noTimeout = vampireProcess?.waitFor(timeLimit, TimeUnit.SECONDS)

            if (noTimeout?.not() == true) {
                echo("timeout")
                vampireProcess.destroy()
                if (vampireProcess.isAlive) vampireProcess.destroyForcibly()
                return@runBlocking
            }

            val vampireParsingResult = vampireProcess?.inputReader()?.useLines { vampireOutput ->
                FOLAnswerTupleToRDFSurfaceController().questionAnsweringOutputToRDFSurfacesCasc(
                    querySurface = parseResult.querySurfaces.single(),
                    debug = commonOptions.debug,
                    questionAnsweringOutputLines = vampireOutput,
                )
            }

            when (vampireParsingResult) {
                null -> {
                    echoError("Vampire Execution Error")
                    throw ProgramResult(1)
                }

                is AnswerTupleToRDFSurfacesGraphSuccess -> echo(vampireParsingResult.value)
                is AnswerTupleToRDFSurfacesGraphFailure -> {
                    echoError(vampireParsingResult.failureMessage)
                    throw ProgramResult(1)
                }
            }
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
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
    RdfSurfaceToFol().subcommands(
        Transform(),
        Check(),
        TransformWithQA(),
        Rewrite(),
        TPTPTupleAnswerToRDFSurfaces()
    )
        .main(args)


fun CliktCommand.echoError(string: String) {
    echo(
        errorKeyWordTextStyle("Error: ") + string,
        err = true
    )
}

fun CliktCommand.echoNonQuiet(string: String) {
    echo("%  $string")
}


