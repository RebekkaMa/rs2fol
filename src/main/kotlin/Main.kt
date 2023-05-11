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
import java.io.File


class Hello : CliktCommand() {
    val path by option(help = "The path to the RDF Surface Graph").file().prompt("The path of your RDF Surface Graph")
    val answerPath by argument(help = "The path to the solution").file().optional()
    val vampireFilePath by argument(help = "The path to the solution").file()
        .default(File("/home/rebekka/Programs/vampire/bin/"))
    val short by option("--short", "-s", help = "Short output").flag(default = false)

    override fun run() {

        val computedAnswerFile =
            answerPath ?: File(path.parentFile.path + "/" + path.nameWithoutExtension + "-answer.n3")

        fun readFile(sourceFile: File): Pair<Map<String, String>, String> {
            val prefixMap = mutableMapOf<String, String>()
            var graph = ""
            sourceFile.bufferedReader().lines().use { lines ->
                for (it in lines) {
                    when {
                        it.contains("^\\s*@prefix ".toRegex()) -> prefixMap.putAll(PrefixParser.parseToEnd(it))
                        it.contains("^\\s*#".toRegex()) -> continue
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

        val (parseError, parseResultValue) = if (graph.isBlank()) {
            false to N3sToFolParser.createFofAnnotatedAxiom("\$true")
        } else {
            val nonPrefixGraph = replacePrefix(prefixMap, graph)
            when (val parserResult = N3sToFolParser.tryParseToEnd(nonPrefixGraph)) {
                is Parsed -> {
                    false to N3sToFolParser.createFofAnnotatedAxiom(parserResult.value)
                }
                is ErrorResult -> {
                    true to ParseException(parserResult).stackTraceToString()
                }
            }
        }


        val (answerParseError, answerParseResultValue) = if (answerGraph.isBlank()) {
            false to N3sToFolParser.createFofAnnotatedConjecture("\$true")
        } else {
            val nonPrefixAnswerGraph = replacePrefix(answerPrefixMap, answerGraph)
            when (val answerParserResult = N3sToFolParser.tryParseToEnd(nonPrefixAnswerGraph)) {
                is Parsed -> {
                    //TODO("beetle7.n3")
                    false to N3sToFolParser.createFofAnnotatedConjecture(answerParserResult.value)
                }
                is ErrorResult -> {
                    true to ParseException(answerParserResult).stackTraceToString()
                }
            }
        }

        val resultString =
            if (parseError) ("Failed to parse " + path.name + ":\n" + parseResultValue + "\n") else "" + if (answerParseError) ("Failed to parse " + (computedAnswerFile.name) + ":\n" + answerParseResultValue + "\n") else ""

        if (parseError or answerParseError) {
            if (!short) println(resultString)
            println(path.name + "  --->  " + "Transforming Error")
            return
        }

        if (!short) println("Transformation was successful!")

        File("TransformationResults/").mkdir()
        val file = File("TransformationResults/" + path.nameWithoutExtension + ".p")
        file.writeText("$parseResultValue\n$answerParseResultValue")
        val absolutePath = file.absolutePath

        if (!short) {
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

        if (!short) {
            vampireResultString.drop(1).forEach { println(it) }
        }

        println(path.name + "  --->  " + vampireResultString.drop(1).dropLastWhile
        { it.isBlank() }
            .joinToString(separator = " --- "))
    }

}

class test(args: Array<String>) : Runnable {
    override fun run() {
        println(N3sToFolParser.parseToEnd(""))

    }
}

fun String.runCommand(workingDir: File): Process? {
    return ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
}

//fun main(args: Array<String>) = test(args).run()
fun main(args: Array<String>) = Hello().main(args)