import PrefixParser.prefix
import PrefixParser.uri
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import javax.swing.text.html.parser.Parser

fun main(args: Array<String>) {
    println("Hello World!")

    val prefStr = "@prefix log: <http://www.w3.org/2000/10/swap/log#>.\n" +
            "@prefix : <http://example.org/ns#>." +
            "\n"

    val prefMap = PrefixParser.parseToEnd(prefStr)

    var str = ":beetle a :Car.\n" +
            "\n" + ":bob a :Person.\n" +
            "\n" +
            ":grannysmith a :Apple .\n" +
            "\n" +
            ":grannysmith :is :green .\n" +
            "\n" +
            ":mars a :Planet.\n" +
            "\n" +
            "(_:A) log:onNegativeSurface {\n" +
            "    _:A :is :green.\n" +
            "    () log:onNegativeSurface {\n" +
            "        _:A :is :beautiful.\n" +
            "    }.\n" +
            "}.\n" +
            "\n" +
            "(_:A) log:onNegativeSurface {\n" +
            "    () log:onNegativeSurface {\n" +
            "        _:A :is :green.\n" +
            "    }.\n" +
            "    () log:onNegativeSurface {\n" +
            "        _:A :is :beautiful.\n" +
            "    }.\n" +
            "}."



    prefMap.forEach { (prefix, uri) ->
        str = str.replace(regex = Regex("(^\\A|\\s)$prefix(\\w+)")) { matchResult: MatchResult ->
            " " + uri.dropLast(1) + matchResult.groupValues.last() + ">"
        }
    }
    str = str.replace(" a ", " <http://www.w3.org/TR/rdf-schema/> ")

    println(str)


    val res2 = N3Parser.parseToEnd(str)

    println(res2)

}