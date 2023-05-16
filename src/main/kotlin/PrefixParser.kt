import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object PrefixParser : Grammar<Map<String, String>>() {

    val prefixStart by literalToken("@prefix")
    val prefix by regexToken("\\w*:")
    val space by regexToken("\\s*", ignore = true)
    val uri by regexToken("^<[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*>")
    val dot by literalToken(".")

    val prefixSentence by -prefixStart and -oneOrMore(space) and prefix and -oneOrMore(space) and uri and -dot

    val prefixPars by oneOrMore(prefixSentence) map {
        buildMap {
            it.forEach { (prefix, uri) ->
                this[prefix.text] = uri.text
            }
        }
    }

    override val rootParser: Parser<Map<String, String>> by prefixPars
}