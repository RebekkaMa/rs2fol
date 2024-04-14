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
import controller.FolAnswerTupleToRDFSurfaceController
import controller.RDFSurfaceToFOLController
import model.rdf_term.IRI
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

val errorKeyWordTextStyle = TextStyle(color = red)
val workingDir = IRI.from("file://" + System.getProperty("user.dir") + "/")


class Rs2fol : CliktCommand() {
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
        help = "Use the rdf:first/rdf:rest list structure to translate lists.\nIf set to \"false\", lists will be translated using FOL functions."
    ).flag(default = false, defaultForHelp = "false")
}

class Rewrite : CliktCommand(help = "Parses and returns an RDF surface using a sublanguage of Notation 3 ") {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get RDF surface from <path>").path().default(Path("-"))
    private val output by option("--output", "-o", help = "Write output to <path>").path().default(Path("-"))

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
            val result = RDFSurfaceToFOLController().transformRDFSurfaceToNotation3(
                rdfSurface = rdfSurface, rdfList = commonOptions.rdfList, baseIRI = baseIRI
            )

            result.fold(
                onSuccess = { rewrittenRdfSurface ->
                    if (output.pathString == "-") {
                        echo(rewrittenRdfSurface)
                        return
                    }
                    output.let {
                        it.parent?.createDirectories()
                        if (it.exists().not()) it.createFile()
                        it.writeText(rewrittenRdfSurface)
                    }
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

class Transform : CliktCommand(help = "Transforms an RDF surface (--input) to a FOL formula in TPTP format") {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get RDF surface from <path>").path()
    private val output by option("--output", "-o", help = "Write output to <path>").path().default(Path.of("-"))

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
            val result = RDFSurfaceToFOLController().transformRDFSurfaceToFOL(
                rdfSurface,
                ignoreQuerySurface,
                rdfList = commonOptions.rdfList,
                baseIRI = baseIri
            )

            result.fold(
                onSuccess = { (folFormula, _) ->
                    if (output.pathString == "-") {
                        echo(folFormula)
                        return
                    }
                    output.let {
                        it.parent?.createDirectories()
                        if (it.exists().not()) it.createFile()
                        it.writeText(folFormula)
                    }
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

class QaAnswerToRs :
    CliktCommand(help = "Transforms a TPTP tuple answer (--input) into an RDF surface using a specified RDF query or question surface (--q-surface)") {
    private val commonOptions by CommonOptions()
    private val input by option("--input", "-i", help = "Get TPTP answer tuple from <path>").path().default(Path("-"))
    private val output by option("--output", "-o", help = "Write output to <path>").path(mustExist = false).default(Path("-"))

    private val querySurface by option("--q-surface", "-q", help = "Get RDF query or question surface from <path>").path(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true
    ).required()


    private val inputType by option("--input-type", "-it").choice("raw", "szs").default("szs")

    private val folAnswerTupleToRDFSurfaceController = FolAnswerTupleToRDFSurfaceController()

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

            val qSurface = folAnswerTupleToRDFSurfaceController.getQuerySurfaceFromRdfSurface(
                rdfSurfaces,
                baseIri,
                commonOptions.rdfList
            ).fold(
                onSuccess = { querySurface ->
                    if (querySurface.isEmpty()) {
                        echoError("--q-surface: RDF surface contains no query or question surface.")
                        throw ProgramResult(1)
                    }

                    if (querySurface.size > 1) {
                        echoError("--q-surface: Multiple query or question surfaces are not supported.")
                        throw ProgramResult(1)
                    }
                    querySurface.single()
                },
                onFailure = { throwable ->
                    val failureMessage = if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message
                        ?: throwable.toString())
                    echoError("--q-surface: $failureMessage")
                    throw ProgramResult(1)
                }
            )


            val result = if (inputType == "raw") {
                folAnswerTupleToRDFSurfaceController.transformTPTPTupleAnswerToRDFSurfaces(
                    tptpTupleAnswer = inputStream.bufferedReader().use { it.readText() },
                    qSurface = qSurface,
                )
            } else {
                inputStream.bufferedReader().useLines {
                    folAnswerTupleToRDFSurfaceController.questionAnsweringOutputToRDFSurfacesCasc(
                        qSurface = qSurface,
                        questionAnsweringOutputLines = it,
                    )
                }
            }

            result.fold(
                onSuccess = { succRes ->
                    if (output.pathString == "-") {
                        echo(succRes)
                        return
                    }
                    output.let {
                        it.parent?.createDirectories()
                        if (it.exists().not()) it.createFile()
                        it.writeText(succRes)
                    }
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
    CliktCommand(help = "Checks whether an RDF surface (--consequence) is a logical consequence of another RDF surface (--input) using the FOL-based Vampire theorem prover") {
    private val commonOptions by CommonOptions()

    private val input by option(
        "--input", "-i",
        help = "Get RDF surface from <path>"
    ).path().default(Path("-"))

    private val consequence by option(
        "--consequence", "-c",
        help = "Path to the consequence (given as RDF surface) (default: <input-parent>/out/<input-name>)"
    ).path()

    private val vampireExec by option(
        "--vampire-exec",
        "-e",
        help = "Path to the Vampire executable"
    ).path(mustExist = true).required()
    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)
    private val output by option(
        "--output",
        "-o",
        help = "Write the generated FOL formula (interim result) to <path>"
    ).path(mustExist = false)

    private val vampireOption by option("--vampire-option-mode", "-v").choice("0", "1").int().default(0)

    private val timeLimit by option("--time-limit", "-t", help = "Time limit in seconds").long().default(120)
        .validate { it > 0 }


    override fun run() {
        try {

            val computedConsequence: Path

            val (inputStream, baseIri) = when {
                input.pathString == "-" -> {
                    computedConsequence = when {
                        consequence == null -> throw BadParameterValue(currentContext.localization.missingOption("The option 'consequence' must not be null if the RDF surface is entered as a stream."))
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
                    System.`in` to workingDir
                }

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

                else -> {
                    computedConsequence = when {
                        consequence == null -> Path(input.parent.pathString + "/out/" + input.name).takeIf { it.exists() }
                            ?: Path(input.pathString + ".out").takeIf { it.exists() }
                            ?: throw BadParameterValue(
                                currentContext.localization.pathDoesNotExist(
                                    "file",
                                    input.pathString + ".out"
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
                    input.inputStream() to IRI.from("file://" + input.absolute().parent.invariantSeparatorsPathString + "/")
                }
            }

            val rdfSurfaceToFOLController = RDFSurfaceToFOLController()

            val rdfSurface = inputStream.bufferedReader().use { it.readText() }
            val rdfSurfaceConsequence = computedConsequence.readText()

            val parseResult = rdfSurfaceToFOLController.transformRDFSurfaceToFOL(
                rdfSurface,
                ignoreQuerySurface = true,
                rdfList = commonOptions.rdfList,
                baseIRI = baseIri
            )
            val answerParseResult = rdfSurfaceToFOLController.transformRDFSurfaceToFOLConjecture(
                rdfSurfaceConsequence,
                rdfList = commonOptions.rdfList,
                baseIRI = baseIri
            )

            val folAtom = parseResult.fold(
                onSuccess = { (folAtom, _) ->
                    if (quiet.not()) echoNonQuiet("Transformation of RDF surface (--input) was successful")
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
                    if (quiet.not()) echoNonQuiet("Transformation of RDF surface (--consequence) was successful")
                    folConjecture
                },
                onFailure = { throwable ->
                    val failureMessage = if (commonOptions.debug) throwable.stackTraceToString() else (throwable.message
                        ?: throwable.toString())
                    echoError("--consequence: $failureMessage")
                    throw ProgramResult(1)
                }
            )


            output?.let {
                it.parent?.createDirectories()
                if (it.exists().not()) it.createFile()
                it.writeText(folAtom + System.lineSeparator() + folConjecture)
                if (!quiet) echoNonQuiet("Problem output file: $output")
            }

            if (!quiet) echoNonQuiet("Starting Vampire...")

            val vampirePrompt =
                if (vampireOption == 0) "$vampireExec --output_mode smtcomp -t ${timeLimit + 5}s" else {
                    "$vampireExec -sa discount -awr 2 -s 1 -add large -afr on -afp 1000 -afq 2.0 -anc none -gsp on -lcm predicate -nm 64 -newcnf on -nwc 5 -sac on -urr ec_only -updr off --output_mode smtcomp -t ${timeLimit + 5}s"
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
                ?.let { echo(if (it.startsWith('s')) TextColors.rgb("#ff9933")("false") else TextColors.green("true")); return }
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

class TransformQa :
    CliktCommand(help = "Transforms an RDF surface to FOL and returns the results of the Vampire question answering feature as an RDF surface") {
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


    private val vampireExecFile by option("--vampire-exec", "-e", help = "File to the Vampire executable").path(
        mustExist = true
    ).required()

    private val quiet by option("--quiet", "-q", help = "Display less output").flag(default = false)

    private val vampireOption by option("--vampire-option-mode", "-v").choice("0", "1").int().default(0)

    private val timeLimit by option("--time-limit", "-t", help = "Time limit in seconds").long().default(60)
        .validate { it > 0 }


    override fun run()  {
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

            val parseResult = RDFSurfaceToFOLController().transformRDFSurfaceToFOL(
                inputStream.reader().use { it.readText() },
                false,
                rdfList = commonOptions.rdfList,
                baseIRI = baseIRI
            )

            val (fol, qSurface) = parseResult.fold(
                onSuccess = { (fol, qSurfaces) ->
                    if (!quiet) echoNonQuiet("Transformation to FOL was successful!")
                    if (qSurfaces.isEmpty()) {
                        echoError("--input: RDF surface contains no query or question surface.")
                        throw ProgramResult(1)
                    }

                    if (qSurfaces.size > 1) {
                        echoError("--input: Multiple query or question surfaces are not supported.")
                        throw ProgramResult(1)
                    }
                    fol to qSurfaces.single()
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
                if (vampireOption == 0) "$vampireExecFile -av off -qa answer_literal -om smtcomp -t ${timeLimit + 1}s" else {
                    "$vampireExecFile -av off -sa discount -s 1 -add large -afp 4000 -afq 1.0 -anc none -gs on -gsem off -inw on -lcm reverse -lwlo on -nm 64 -nwc 1 -sas z3 -sos all -sac on -thi all -uwa all -updr off -uhcvi on -to lpo -qa answer_literal -om smtcomp -t ${timeLimit + 1}s"
                }

            val vampireProcess = vampirePrompt.runCommand(File(System.getProperty("user.dir")))
            vampireProcess.outputWriter().use { it.write(fol) }

            val noTimeout = vampireProcess.waitFor(timeLimit, TimeUnit.SECONDS)

            if (noTimeout.not()) {
                echo("timeout")
                vampireProcess.destroy()
                if (vampireProcess.isAlive) vampireProcess.destroyForcibly()
                return
            }

            val vampireParsingResult = vampireProcess.inputReader().useLines { vampireOutput ->
                FolAnswerTupleToRDFSurfaceController().questionAnsweringOutputToRDFSurfacesCasc(
                    qSurface = qSurface,
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
    Rs2fol().subcommands(
        Rewrite(),
        Transform(),
        QaAnswerToRs(),
        Check(),
        TransformQa(),
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
