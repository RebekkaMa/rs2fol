import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object PrefixParser : Grammar<Map<String, String>>() {

    val space by regexToken("\\s+", ignore = true)

    val dot by literalToken(".")
    val minus by literalToken("-")

    val prefixIdStart by literalToken("@prefix")
    val baseStart by literalToken("@base")
    val sparqlBaseStart by literalToken("BASE")
    val sparqlPrefixStart by literalToken("PREFIX")

    val pnCharsBase = "([A-Z]|[a-z]|[\u00C0-\u00D6]|[\u00D8-\u00F6]|[\u00F8-\u02FF]|[\u0370-\u037D]|[\u037F-\u1FFF]|[\u200C-\u200D]|[\u2070-\u218F]|[\u2C00-\u2FEF]|[\u3001-\uD7FF]|[\uF900-\uFDCF]|[\uFDF0-\uFFFD]|[\uD800\uDC00-\uDB7F\uDFFF])".toRegex()
    val pnCharsU = "($pnCharsBase|_)".toRegex()
    val pnChars = "($pnCharsU|-|[0-9]|\u00B7|[\u0300-\u036F]|[\u203F-\u2040])".toRegex()

    val pnPrefix = "($pnCharsBase(($pnChars|\\.)*$pnChars)?)".toRegex()

    val iriref by regexToken("<([^\u0000-\u0020<>\"{}|^`\\\\]|(((\\\\u([0-9]|[A-F]|[a-f]){4})|(\\\\U([0-9]|[A-F]|[a-f]){8}))))*>")

    val pnameNs by regexToken("$pnPrefix?:")

    //val pname_ns by regexToken("(([A-Z]|[a-z]|[À-Ö]|[Ø-ö]|[ø-˿]|[Ͱ-ͽ]|[Ϳ-\u1FFF]|[\u200C-\u200D]|[⁰-\u218F]|[Ⰰ-\u2FEF]|[、-\uD7FF]|[豈-\uFDCF]|[ﷰ-�]|[\uD800\uDC00-\uDB7F\uDFFF])((((([A-Z]|[a-z]|[À-Ö]|[Ø-ö]|[ø-˿]|[Ͱ-ͽ]|[Ϳ-\u1FFF]|[\u200C-\u200D]|[⁰-\u218F]|[Ⰰ-\u2FEF]|[、-\uD7FF]|[豈-\uFDCF]|[ﷰ-�]|[\uD800\uDC00-\uDB7F\uDFFF])|_)|-|([0-9]|·|[̀-ͯ]|[‿-⁀]))|\\.)*((([A-Z]|[a-z]|[À-Ö]|[Ø-ö]|[ø-˿]|[Ͱ-ͽ]|[Ϳ-\u1FFF]|[\u200C-\u200D]|[⁰-\u218F]|[Ⰰ-\u2FEF]|[、-\uD7FF]|[豈-\uFDCF]|[ﷰ-�]|[\uD800\uDC00-\uDB7F\uDFFF])|_)|-|([0-9]|·|[̀-ͯ]|[‿-⁀])))?)?:")
    //val iriref by regexToken("<([^\u0000-\u0020<>\"{}|^`\\\\]|(((\\\\u([0-9]|[A-F]|[a-f]){4})|(\\\\U([0-9]|[A-F]|[a-f]){8}))))*>")

    val prefixID by -prefixIdStart and pnameNs and iriref and -dot
    val base by -baseStart and iriref and -dot
    val sparqlBase by -sparqlBaseStart and iriref
    val sparqlPrefix by -sparqlPrefixStart and pnameNs and iriref


    val prefixPars by oneOrMore(prefixID or sparqlPrefix) map {
        buildMap {
            it.forEach { (prefix, uri) ->
                this[prefix.text] = uri.text
            }
        }
    }

    override val rootParser: Parser<Map<String, String>> by prefixPars
}