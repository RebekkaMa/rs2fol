import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parsed
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit


class Hello : CliktCommand() {
    val path by option(help = "The path to the RDF Surface Graph").file().prompt("The path of your RDF Surface Graph")
    val answerPath by argument(help = "The path to the solution").file().optional()
    val vampireFilePath by argument(help = "The path to the solution").file()
        .default(File("/home/rebekka/Programs/vampire/bin/"))
    val short by option("--on", "-o", help = "Short output").flag(default = false)

    override fun run() {

        val computedAnswerFile =
            answerPath ?: File(path.parentFile.path + "/" + path.nameWithoutExtension + "-answer.n3")

        fun readFile(sourceFile: File): Pair<Map<String, String>, String> {
            val prefixMap = mutableMapOf<String, String>()
            var graph = ""
            sourceFile.bufferedReader().lines().use { lines ->
                for (it in lines) {
                    when {
                        it.startsWith("@prefix ") -> prefixMap.putAll(PrefixParser.parseToEnd(it))
                        it.startsWith("#") -> continue
                        it.contains("onQuerySurface") -> break
                        else -> graph = graph + "\n" + it
                    }
                }
            }
            return prefixMap to graph
        }

        fun replacePrefix(prefixMap: Map<String, String>, graph: String): String {
            var newGraph = graph
            prefixMap.forEach { (prefix, uri) ->
                newGraph =
                    newGraph.replace(regex = Regex("(^\\A|\\s)$prefix([^\\s\\.,;]+)")) { matchResult: MatchResult ->
                        " " + uri.dropLast(1) + matchResult.groupValues.last() + ">"
                    }
            }
            newGraph = newGraph.replace(" a ", " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ")
            return newGraph
        }

        val (prefixMap, graph) = readFile(path)
        val (answerPrefixMap, answerGraph) = readFile(computedAnswerFile)

        val parserResult = N3sToFolParser.tryParseToEnd(replacePrefix(prefixMap, graph))
        val answerParserResult = N3sToFolParser.tryParseToEnd(replacePrefix(answerPrefixMap, answerGraph))

        val (parseError, parseResultValue) = when (parserResult) {
            is Parsed -> {
                false to "fof(axiom,axiom," + (if (graph.isBlank()) "\$true" else parserResult.value) + ")."
            }

            is ErrorResult -> {
                true to ParseException(parserResult).stackTraceToString()
            }
        }

        val (answerParseError, answerParseResultValue) = when (answerParserResult) {
            is Parsed -> {
                //TODO("beetle7.n3")
                false to "fof(conjecture,conjecture," + (if (answerGraph.isBlank()) "\$true" else answerParserResult.value) + ")."

            }

            is ErrorResult -> {
                true to answerParserResult.toString()
            }
        }

        val resultString =
            if (parseError) ("Failed to parse " + path.name + ":\n" + parseResultValue + "\n") else "" + if (answerParseError) ("Failed to parse " + (computedAnswerFile.name) + ":\n" + parseResultValue + "\n") else ""

        if (parseError or answerParseError) {
            if (!short) println(resultString)
            println(path.name + "  --->  " + "Transforming Error")
            return
        }

        val file = File(path.nameWithoutExtension + ".p")
        file.writeText("$parseResultValue\n$answerParseResultValue")
        val absolutePath = file.absolutePath

        if (!short) {
            println("Transformation was successful!")
            println("Problem output file: " + file.path)
            println("Starting Vampire...")
        }

        val vampireProcess = "./vampire --output_mode smtcomp $absolutePath".runCommand(vampireFilePath)

        vampireProcess?.waitFor()

        val vampireResultString = vampireProcess?.inputStream?.reader()?.readText()?.lines()

        if (vampireResultString == null) {
            println(path.name + "  --->  " + "Vampire Error")
            return
        }

        if (!short){
            vampireResultString.drop(1).forEach { println(it) }
        }

        println( path.name + "  --->  " + vampireResultString.last { it.isNotBlank() })
    }

}

fun main(args: Array<String>) = Hello().main(args)

fun String.runCommand(workingDir: File): Process? {
    return ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
}