
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextStyle
import controller.FOLAnswerTupleToRDFSurfaceController
import controller.RDFSurfaceToFOLController
import kotlinx.coroutines.runBlocking
import rdfSurfaces.IRI
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

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
    ).flag(default = false, defaultForHelp = "false")
}

class Rewrite : CliktCommand(help = "Parses and prints an RDF surfaces graph using a Notation 3 sublanguage") {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get RDF surfaces graph from <path>").path()
    private val output by option("--output", "-o", help = "Write output to <path>").path()

    override fun run() {

        try {
            val (inputStream, baseIRI) = when {
                input == null || input!!.pathString == "-" -> System.`in` to IRI.from("file://" + System.getProperty("user.dir") + "/")

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

            val rdfSurfacesGraph = inputStream.bufferedReader().use { it.readText() }
            val result = RDFSurfaceToFOLController().transformRDFSurfaceGraphToNotation3(
                rdfSurfaceGraph = rdfSurfacesGraph, rdfLists = commonOptions.rdfList, baseIRI = baseIRI
            )

            result.fold(
                onSuccess = { rewrittenRdfSurfacesGraph ->
                    output?.let {
                        it.parent?.createDirectories()
                        if (it.exists().not()) it.createFile()
                        it.writeText(rewrittenRdfSurfacesGraph)
                    } ?: echo(rewrittenRdfSurfacesGraph)
                },
                onFailure = { throwable ->
                    val failureMessage =  if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message
                            ?: throwable.toString())
                    echoError(failureMessage)
                    throw ProgramResult(1)
                }
            )
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}

class Transform : CliktCommand(help = "Transforms an RDF surfaces graph (--input) to a FOL formula in TPTP format") {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get RDF surfaces graph from <path>").path()
    private val output by option("--output", "-o", help = "Write output to <path>").path()

    private val ignoreQuerySurface by option("--ignoreQuerySurface").flag(default = false, defaultForHelp = "false")

    override fun run() {

        try {
            val (inputStream, baseIRI) = when {
                input == null || input!!.pathString == "-" -> System.`in` to IRI.from("file://" + System.getProperty("user.dir") + "/")

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

            val rdfSurfacesGraph = inputStream.bufferedReader().use { it.readText() }
            val result = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                rdfSurfacesGraph,
                ignoreQuerySurface,
                rdfLists = commonOptions.rdfList,
                baseIRI = baseIRI
            )

            result.fold(
                onSuccess = { (folFormula, _) ->
                    output?.let {
                        it.parent?.createDirectories()
                        if (it.exists().not()) it.createFile()
                        it.writeText(folFormula)
                    } ?: echo(folFormula)
                },
                onFailure = { throwable ->
                    val failureMessage =  if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message
                            ?: throwable.toString())
                    echoError(failureMessage)
                    throw ProgramResult(1)
                }
            )
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}

class TPTPTupleAnswerToRDFSurfaces :
    CliktCommand(help = "Transforms a TPTP tuple answer (--input) into an RDF surfaces graph using a specified RDF query surface (--query-surface)") {
    private val commonOptions by CommonOptions()
    private val input by option("--tuple-answer", "-t", help = "Get tptp answer tuple from <path>").path()
    private val output by option("--output", "-o", help = "Write output to <path>").path(mustExist = false)

    private val querySurface by option("--query-surface", "-s", help = "Get RDF query surface from <path>").path(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).required()


    private val inputType by option("--input-option", "-io").choice("raw", "szs").default("szs")

    private val fOLAnswerTupleToRDFSurfaceController = FOLAnswerTupleToRDFSurfaceController()

    override fun run() {

        try {
            val (inputStream, baseIRI) = when {
                input == null || input!!.pathString == "-" -> System.`in` to IRI.from("file://" + System.getProperty("user.dir") + "/")

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

            val rdfSurfaceGraph = querySurface.readText()

            val querySurface = fOLAnswerTupleToRDFSurfaceController.getQuerySurfaceFromRdfSurfacesGraph(rdfSurfaceGraph, commonOptions.rdfList).fold(
                onSuccess = {querySurface ->
                    if (querySurface.isEmpty()) {
                        echoError("--query-surface: RDF surfaces graph contains no query surface.")
                        throw ProgramResult(1)
                    }

                    if (querySurface.size > 1){
                        echoError("--query-surface: Multiple query surfaces are not supported.")
                        throw ProgramResult(1)
                    }
                    querySurface.single()
                },
                onFailure = {throwable ->
                    val failureMessage =  if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message ?: throwable.toString())
                    echoError("--query-surface: $failureMessage")
                    throw ProgramResult(1)
                }
            )


            val result = if (inputType == "raw") {
                fOLAnswerTupleToRDFSurfaceController.transformTPTPTupleAnswerToRDFSurfaces(
                    tptpTupleAnswer = inputStream.bufferedReader().use { it.readText() },
                    querySurface = querySurface,
                )
            } else {
                inputStream.bufferedReader().useLines {
                    fOLAnswerTupleToRDFSurfaceController.questionAnsweringOutputToRDFSurfacesCasc(
                        querySurface = querySurface,
                        questionAnsweringOutputLines = it,
                    )
                }
            }

            result.fold(
                onSuccess = { succRes ->
                    output?.let {
                        it.parent?.createDirectories()
                        if (it.exists().not()) it.createFile()
                        it.writeText(succRes)
                    } ?: echo(succRes)
                },
                onFailure = { throwable ->
                    val failureMessage = if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message
                            ?: throwable.toString())
                    echoError(failureMessage)
                    throw ProgramResult(1)
                }
            )
        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}


class Check :
    CliktCommand(help = "Checks whether an RDF surfaces graph (--consequence) is a logical consequence of another RDF surfaces graph (--input), using the FOL-based Vampire Theorem Proofer") {
    private val commonOptions by CommonOptions()

    private val input by option(
        "--input", "-i",
        help = "Path to RDF surfaces graph"
    ).path()

    private val consequence by option(
        "--consequence", "-c",
        help = "Path to the consequence (given as RDF surfaces graph) (default: <<input>-answer.n3s>)"
    ).path()

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

            var baseIRI: IRI
            var computedExpectedAnswer: Path

            val inputStream = when {
                input == null || input!!.pathString == "-" -> {
                    baseIRI = IRI.from("file://" + System.getProperty("user.dir") + "/")
                    computedExpectedAnswer = when {
                        consequence == null -> throw BadParameterValue(currentContext.localization.missingOption("option 'consequences' must not be null if the RDF graph is given as a stream."))
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
                    computedExpectedAnswer = when {
                        consequence == null -> Path(input!!.parent.pathString + "/" + input!!.nameWithoutExtension + "-answer.n3s").takeIf { it.exists() }
                            ?: Path(input!!.parent.pathString + "/" + input!!.nameWithoutExtension + "-answer.n3").takeIf { it.exists() }
                            ?: throw BadParameterValue(
                                currentContext.localization.pathDoesNotExist(
                                    "file",
                                    input!!.parent.pathString + "/" + input!!.nameWithoutExtension + "-answer.n3s"
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
                    input!!.inputStream()
                }
            }

            val rdfSurfaceToFOLController = RDFSurfaceToFOLController()

            val graph = inputStream.bufferedReader().use { it.readText() }
            val answerGraph = computedExpectedAnswer.readText()

            val parseResult = rdfSurfaceToFOLController.transformRDFSurfaceGraphToFOL(
                graph,
                ignoreQuerySurface = true,
                rdfLists = commonOptions.rdfList,
                baseIRI = baseIRI
            )
            val answerParseResult = rdfSurfaceToFOLController.transformRDFSurfaceGraphToFOLConjecture(
                answerGraph,
                rdfLists = commonOptions.rdfList,
                baseIRI = baseIRI
            )

            val folAtom = parseResult.fold(
                onSuccess = { (folAtom, _) ->
                    if (quiet.not()) echoNonQuiet("Transformation of --input RDF surfaces graph was successful")
                    folAtom
                },
                onFailure = { throwable ->
                    val failureMessage = if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message
                            ?: throwable.toString())
                    echoError("--input: $failureMessage")
                    throw ProgramResult(1)
                }
            )

            val folConjecture = answerParseResult.fold(
                onSuccess = { folConjecture ->
                    if (quiet.not() && folAtom != null) echoNonQuiet("Transformation of --consequence RDF surfaces graph was successful")
                    folConjecture
                },
                onFailure = { throwable ->
                    val failureMessage = if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message
                            ?: throwable.toString())
                    echoError("--consequence: $failureMessage")
                    throw ProgramResult(1)
                }
            )

            //if (parseResult is Failure || answerParseResult is Failure) throw ProgramResult(1)

            output?.let {
                it.parent?.createDirectories()
                if (it.exists().not()) it.createFile()
                it.writeText(folAtom + System.lineSeparator() + folConjecture)
                if (!quiet) echoNonQuiet("Problem output file: $output")
            }

            if (!quiet) echoNonQuiet("Starting Vampire...")

            val vampirePrompt =
                if (vampireOption == 0) "$vampireExec --output_mode smtcomp" else {
                    "$vampireExec -sa discount -awr 2 -s 1 -add large -afr on -afp 1000 -afq 2.0 -anc none -gsp on -lcm predicate -nm 64 -newcnf on -nwc 5 -sac on -urr ec_only -updr off --output_mode smtcomp"
                }

            val vampireProcess = vampirePrompt.runCommand(File(System.getProperty("user.dir")))
            vampireProcess.outputWriter()
                ?.use { it.write(folAtom + System.lineSeparator() + folConjecture) }

            val noTimeout = vampireProcess.waitFor(timeLimit, TimeUnit.SECONDS)

            if (noTimeout.not()) {
                echo("timeout")
                vampireProcess.destroy()
                if (vampireProcess.isAlive) vampireProcess.destroyForcibly()
                return
            }

            val vampireResultString = vampireProcess.inputReader().readLines()

            vampireResultString.lastOrNull { it == "sat" || it == "unsat" }
                ?.let { echo(if (it.startsWith('s')) TextColors.rgb("#ff9933")(it) else TextColors.green(it)); return }
            vampireResultString.lastOrNull { it.contains("error", true) }?.let { echoError(it); return }

            echoError(
                "Unknown Vampire output" + if (commonOptions.debug) vampireResultString.joinToString(
                    prefix = "Vampire output:${System.lineSeparator()}",
                    separator = System.lineSeparator()
                ) else ""
            )
            throw ProgramResult(1)

        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}

class TransformWithQA :
    CliktCommand(help = "Transforms an RDF surfaces graph to FOL and returns the results of the Vampire question answering feature") {
    private val commonOptions by CommonOptions()
    private val input by option(
        "--input", "-i",
        help = "File to RDF Surfaces graph"
    ).path()
    private val output by option(
        "--output",
        "-o",
        help = "Write transformation output to <path>"
    ).path()


    private val vampireExecFile by option(help = "File to vampire executable").path()
        .default(Path("/home/rebekka/Programs/Vampire-qa/tmp/build/bin/vampire_z3_rel_qa_6176"))

    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)

    private val vampireOption by option("--vampire-option", "-v").choice("0", "1").int().default(0)

    private val timeLimit by option("--time-limit", "-t", help = "Reasoning time limit in seconds").long().default(60)
        .validate { it > 0 }


    override fun run() = runBlocking {
        try {
            var baseIRI: IRI

            val inputStream = when {
                input == null || input!!.pathString == "-" -> {
                    baseIRI = IRI.from("file://" + System.getProperty("user.dir") + "/")
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

            val parseResult = RDFSurfaceToFOLController().transformRDFSurfaceGraphToFOL(
                inputStream.reader().use { it.readText() },
                false,
                rdfLists = commonOptions.rdfList,
                baseIRI = baseIRI
            )

            val (fol, querySurface) = parseResult.fold(
                onSuccess = { (fol, querySurfaces) ->
                    if (!quiet) echoNonQuiet("Transformation to FOL was successful!")
                    if (querySurfaces.isEmpty()) {
                        echoError("--input: RDF surfaces graph contains no query surface.")
                        throw ProgramResult(1)
                    }

                    if (querySurfaces.size > 1){
                        echoError("--input: Multiple query surfaces are not supported.")
                        throw ProgramResult(1)
                    }
                    fol to querySurfaces.single()
                },
                onFailure = { throwable ->
                    val failureMessage = if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message
                            ?: throwable.toString())
                    echoError("--input: $failureMessage")
                    throw ProgramResult(1)
                }
            )

            output?.let {
                it.parent?.createDirectories()
                if (it.exists().not()) it.createFile()
                it.writeText(fol)
                if (!quiet) echoNonQuiet("Problem output file: $output")
            }

            if (!quiet) echoNonQuiet("Starting Vampire...")

            val vampirePrompt =
                if (vampireOption == 0) "$vampireExecFile -av off -qa answer_literal -om smtcomp" else {
                    "$vampireExecFile -av off -sa discount -s 1 -add large -afp 4000 -afq 1.0 -anc none -gs on -gsem off -inw on -lcm reverse -lwlo on -nm 64 -nwc 1 -sas z3 -sos all -sac on -thi all -uwa all -updr off -uhcvi on -to lpo -qa answer_literal -om smtcomp"
                }

            val vampireProcess = vampirePrompt.runCommand(File(System.getProperty("user.dir")))
            vampireProcess.outputWriter().use { it.write(fol) }

            val noTimeout = vampireProcess.waitFor(timeLimit, TimeUnit.SECONDS)

            if (noTimeout.not()) {
                echo("timeout")
                vampireProcess.destroy()
                if (vampireProcess.isAlive) vampireProcess.destroyForcibly()
                return@runBlocking
            }

            val vampireParsingResult = vampireProcess.inputReader().useLines { vampireOutput ->
                FOLAnswerTupleToRDFSurfaceController().questionAnsweringOutputToRDFSurfacesCasc(
                    querySurface = querySurface,
                    questionAnsweringOutputLines = vampireOutput,
                )
            }

            vampireParsingResult.fold(
                onSuccess = {
                    echo(it)
                },
                onFailure = { throwable ->
                    val failureMessage = if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message
                            ?: throwable.toString())
                    echoError(failureMessage)
                    throw ProgramResult(1)
                }
            )

        } catch (exception: Exception) {
            if (exception is CliktError) throw exception
            if (commonOptions.debug) echoError(exception.stackTraceToString()) else echoError(exception.toString())
            throw ProgramResult(1)
        }
    }
}

fun String.runCommand(workingDir: File): Process {
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


