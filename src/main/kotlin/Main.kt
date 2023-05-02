import PrefixParser.prefix
import PrefixParser.uri
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.file
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import java.io.File
import javax.swing.text.html.parser.Parser

class Hello : CliktCommand() {
    val path by option(help = "The path to the RDF Surface Graph").file().prompt("The path of your RDF Surface Graph")
    override fun run() {

        val prefMap = mutableMapOf<String, String>()
        var graph = ""

        path.bufferedReader().lines().use {lines ->
            for (it in lines){
                when {
                    it.startsWith("@prefix ") ->  prefMap.putAll(PrefixParser.parseToEnd(it))
                    it.contains("query") -> break
                    else -> graph = graph + "\n" + it
                }
            }
        }

        prefMap.forEach { (prefix, uri) ->
            graph = graph.replace(regex = Regex("(^\\A|\\s)$prefix(\\w+)")) { matchResult: MatchResult ->
                " " + uri.dropLast(1) + matchResult.groupValues.last() + ">"
            }
        }

        val str = graph.replace(" a ", " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ")

        println(str)

        var res2 = N3Parser.parseToEnd(str)

//    res2 = res2?.replace(regex = Regex("'<(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]>'")) {
//        it.value.drop(2).dropLast(2).replace(regex = Regex("\\W"), "_")
//    }

        println("Result--------------------")
        println(res2)
        println("--------------------------")


    }

}

fun main(args: Array<String>) = Hello().main(args)