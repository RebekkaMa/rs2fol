import N3sToFolParser.iri
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

    val pname_ns by regexToken("(([A-Z]|[a-z]|[À-Ö]|[Ø-ö]|[ø-˿]|[Ͱ-ͽ]|[Ϳ-\u1FFF]|[\u200C-\u200D]|[⁰-\u218F]|[Ⰰ-\u2FEF]|[、-\uD7FF]|[豈-\uFDCF]|[ﷰ-�]|[\uD800\uDC00-\uDB7F\uDFFF])((((([A-Z]|[a-z]|[À-Ö]|[Ø-ö]|[ø-˿]|[Ͱ-ͽ]|[Ϳ-\u1FFF]|[\u200C-\u200D]|[⁰-\u218F]|[Ⰰ-\u2FEF]|[、-\uD7FF]|[豈-\uFDCF]|[ﷰ-�]|[\uD800\uDC00-\uDB7F\uDFFF])|_)|-|([0-9]|·|[̀-ͯ]|[‿-⁀]))|\\.)*((([A-Z]|[a-z]|[À-Ö]|[Ø-ö]|[ø-˿]|[Ͱ-ͽ]|[Ϳ-\u1FFF]|[\u200C-\u200D]|[⁰-\u218F]|[Ⰰ-\u2FEF]|[、-\uD7FF]|[豈-\uFDCF]|[ﷰ-�]|[\uD800\uDC00-\uDB7F\uDFFF])|_)|-|([0-9]|·|[̀-ͯ]|[‿-⁀])))?)?:")
    val iriref by regexToken("<([^\u0000-\u0020<>\"{}|^`\\\\]|(((\\\\u([0-9]|[A-F]|[a-f]){4})|(\\\\U([0-9]|[A-F]|[a-f]){8}))))*>")

    val prefixID by -prefixIdStart and pname_ns and iriref and -dot
    val base by -baseStart and iriref and -dot
    val sparqlBase by -sparqlBaseStart and iriref
    val sparqlPrefix by -sparqlPrefixStart and pname_ns and iriref


    val prefixPars by oneOrMore(prefixID or sparqlPrefix) map {
        buildMap {
            it.forEach { (prefix, uri) ->
                this[prefix.text] = uri.text
            }
        }
    }

    override val rootParser: Parser<Map<String, String>> by prefixPars
}