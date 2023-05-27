import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parsed
import java.io.File


class RdfSurfaceToFol : CliktCommand() {
    val axiomFile by option(help = "Path of the file with the RDF Surface graph").file()
        .prompt("Path of the file with the RDF Surface graph")
    val conjectureFile by argument(help = "Path of the file with the expected solution").file().optional()
    val vampireExecFile by argument(help = "Path of the vampire execution file").file()
        .default(File("/home/rebekka/Programs/vampire/bin/"))
    val short by option("--short", "-s", help = "Short output").flag(default = false)

    override fun run() {

        val computedAnswerFile =
            conjectureFile ?: File(axiomFile.parentFile.path + "/" + axiomFile.nameWithoutExtension + "-answer.n3")

        fun readFile(sourceFile: File): String {
            return buildString {
                sourceFile.bufferedReader().lines().use { lines ->
                    for (it in lines) {
                        when {
                            it.contains("^\\s*#".toRegex()) -> continue
                            else -> this.append(it + "\n")
                        }
                    }
                }
            }
        }

        val graph = readFile(axiomFile)
        val answerGraph = readFile(computedAnswerFile)

        val (parseError, parseResultValue) =
            when (
                val parserResult = N3sToFolParser.tryParseToEnd(graph)) {
                is Parsed -> {
                    false to N3sToFolParser.createFofAnnotatedAxiom(parserResult.value)
                }

                is ErrorResult -> {
                    true to ParseException(parserResult).stackTraceToString()
                }

            }


        val (answerParseError, answerParseResultValue) = when (
            val answerParserResult = N3sToFolParser.tryParseToEnd(answerGraph)) {
            is Parsed -> {
                //TODO("beetle7.n3")
                false to N3sToFolParser.createFofAnnotatedConjecture(answerParserResult.value)
            }

            is ErrorResult -> {
                true to ParseException(answerParserResult).stackTraceToString()
            }

        }

        val resultString =
            if (parseError) ("Failed to parse " + axiomFile.name + ":\n" + parseResultValue + "\n") else "" + if (answerParseError) ("Failed to parse " + (computedAnswerFile.name) + ":\n" + answerParseResultValue + "\n") else ""

        if (parseError or answerParseError) {
            if (!short) println(resultString)
            println(axiomFile.name + "  --->  " + "Transforming Error")
            return
        }

        if (!short) println("Transformation was successful!")

        File("TransformationResults/").mkdir()
        val file = File("TransformationResults/" + axiomFile.nameWithoutExtension + ".p")
        file.writeText("$parseResultValue\n$answerParseResultValue")
        val absolutePath = file.absolutePath

        if (!short) {
            println("Problem output file: " + file.path)
            println("Starting Vampire...")
        }

        val vampireProcess = "./vampire --output_mode smtcomp $absolutePath".runCommand(vampireExecFile)

        vampireProcess?.waitFor()

        val vampireResultString = vampireProcess?.inputStream?.reader()?.readText()?.lines()

        if (vampireResultString == null) {
            println(axiomFile.name + "  --->  " + "Vampire Error")
            return
        }

        if (!short) {
            vampireResultString.drop(1).forEach { println(it) }
        }

        println(axiomFile.name + "  --->  " + vampireResultString.drop(1).dropLastWhile
        { it.isBlank() }
            .joinToString(separator = " --- "))
    }
}

fun String.runCommand(workingDir: File): Process? {
    return ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
}

fun main(args: Array<String>) = RdfSurfaceToFol().main(args)