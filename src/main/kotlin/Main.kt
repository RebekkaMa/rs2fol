import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.io.File
import java.util.concurrent.TimeUnit

class Hello : CliktCommand() {
    val path by option(help = "The path to the RDF Surface Graph").file().prompt("The path of your RDF Surface Graph")
    val answerPath by argument(help = "The path to the solution").file().optional()
    val vampireFilePath by argument(help = "The path to the solution").file().default(File("/home/rebekka/Programs/vampire/bin/"))

    override fun run() {

        fun readFile(sourceFile: File): Pair<Map<String, String>,String>{
            val prefixMap = mutableMapOf<String, String>()
            var graph = ""
            sourceFile.bufferedReader().lines().use {lines ->
                for (it in lines){
                    when {
                        it.startsWith("@prefix ") ->  prefixMap.putAll(PrefixParser.parseToEnd(it))
                        it.startsWith("#") -> continue
                        it.contains("onQuerySurface") -> break
                        else -> graph = graph + "\n" + it
                    }
                }
            }
            return prefixMap to graph
        }

        fun replacePrefix(prefixMap: Map<String, String>, graph: String):String {
            var newGraph = graph
            prefixMap.forEach { (prefix, uri) ->
                newGraph = newGraph.replace(regex = Regex("(^\\A|\\s)$prefix(\\w+)")) { matchResult: MatchResult ->
                    " " + uri.dropLast(1) + matchResult.groupValues.last() + ">"
                }
            }
            newGraph = newGraph.replace(" a ", " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ")
            return newGraph
        }

        val (prefixMap, graph) = readFile(path)
        val (answerPrefixMap, answerGraph) = readFile(answerPath ?: File((path.path.dropLast(3).plus("-answer.n3"))))

        val folFormular = "fof(axiom,axiom," + N3sToFolParser.parseToEnd(replacePrefix(prefixMap,graph)) + ")."
        val folFormularAnswer = "fof(conjecture,conjecture," + N3sToFolParser.parseToEnd(replacePrefix(answerPrefixMap,answerGraph)) + ")."


        val file = File( path.name.dropLastWhile { it != '.'} + "p")
        file.writeText("$folFormular\n$folFormularAnswer")
        val absolutePath = file.absolutePath


        println("Transformation was successful!")
        println("Problem output file: " + file.path)
        println("Starting Vampire")

        "./vampire --output_mode smtcomp $absolutePath".runCommand(vampireFilePath)
    }

}

fun main(args: Array<String>) = Hello().main(args)

fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(60, TimeUnit.MINUTES)
}