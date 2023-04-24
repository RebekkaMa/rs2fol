import N3Parser.variableList
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object N3Parser : Grammar<String?>() {

    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val lparcurl by literalToken("{")
    val rparcurl by literalToken("}")
    val simpleSpace by literalToken(" ", ignore = true)
    val space by regexToken("\\s*", ignore = true)
    val alspace by regexToken("\\s+", ignore = true)
    val dot by literalToken(".")

    val negativeSurfaceText by literalToken("<http://www.w3.org/2000/10/swap/log#onNegativeSurface>")
    val positiveSurfaceText by literalToken("<http://www.w3.org/2000/10/swap/log#onPositiveSurface>")

    val variable by regexToken("_:\\w+\\b")
    val v by variable use { this.text.drop(2) }

    val ressource by regexToken("^<(https?|http|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]>")
    val r by ressource use { "'" + this.text + "'" }

    val triple by (v or r) and -simpleSpace and (v or r) and -simpleSpace and (v or r) use { "triple(" + this.t1 + ", " + this.t2 + ", " + this.t3 + ")" }
    val emptyvarList by (lpar and rpar) use { "" }
    val variableList by emptyvarList or (-lpar and oneOrMore(v and (0..1 times space)) and -rpar).map { it.joinToString(separator = ",") { tuple2 -> tuple2.t1 } }

    val aParser: Parser<String> by
    oneOrMore(
        (variableList and
                -zeroOrMore(v and (0..1 times space)) and
                (negativeSurfaceText or positiveSurfaceText) and
                -(0..1 times space) and
                -lparcurl and
                -(0..1 times space) and
                parser(this::aParser) and
                -(0..1 times space) and
                -rparcurl) map { (variableList, surface, rest) ->
            val variableListNotNull = variableList.isNotEmpty()
            var outputString = ""
            if (surface.type == positiveSurfaceText) {
                if (variableListNotNull) {
                    outputString = "? [$variableList] : "
                }
            } else {
                if (variableListNotNull) {
                    outputString = "! [$variableList] :"
                }
                outputString = "$outputString~"

            }
            outputString += rest
            outputString
        } or
                triple
                and -dot) use { this.joinToString(prefix = "(", postfix = ")", separator = " & ") }

    override val rootParser: Parser<String> by aParser use {"fof(test,axiom,$this)." }
}