import N3sToFolParser.hex
import N3sToFolParser.unicodeBig
import N3sToFolParser.unicodeSmall
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object PrefixParser : Grammar<Map<String, String>>() {

    val dot by literalToken(".")
    val pname_ns by regexToken("\\w*:")
    val space by regexToken("\\s+", ignore = true)
    val hex by regexToken("[0-9]|[A-F]|[a-f]")
    val UCHAR by (unicodeSmall and hex and hex and hex and hex).use { this.t1.text + this.t2.text + this.t3.text + this.t4.text + this.t5.text} or (unicodeBig and hex and hex and hex and hex and hex and hex and hex and hex).use { this.t1.text + this.t2.text + this.t3.text + this.t4.text + this.t5.text + this.t6.text + this.t7.text + this.t8.text + this.t9.text}
    val iri by regexToken("<[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*>")
    val irireftext by regexToken("[^\u0000-\u0020<>\"{}|^`]")
    val iriref by literalToken("<") and zeroOrMore(irireftext.use { this.text } or UCHAR) and literalToken(">") use {this.t1.text + this.t2.joinToString(separator = "") +this.t3.text}

    val pnLocalESC by regexToken("\\\\[-_~.!$&'()*+,;=/?#@%]")
    val percent by literalToken("%") and hex and hex
    val plx by percent or pnLocalESC
    val pnCharsBase by regexToken("[A-Z]|[a-z]|[\u00C0-\u00D6]|[\u00D8-\u00F6]|[\u00F8-\u02FF]|[\u0370-\u037D]|[\u037F-\u1FFF]|[\u200C-\u200D]|[\u2070-\u218F]|[\u2C00-\u2FEF]|[\u3001-\uD7FF]|[\uF900-\uFDCF]|[\uFDF0-\uFFFD]|[\uD800\uDC00-\uDB7F\uDFFF]")
    val pnCharsU by pnCharsBase or literalToken("_")
    val pnChars by pnCharsU or literalToken("-") or regexToken("[0-9]|\u00B7|[\u0300-\u036F]|[\u203F-\u2040]")
    val pnLocal by (pnCharsU or literalToken(":") or regexToken("[0-9]") or plx) and optional(zeroOrMore(pnChars or dot or literalToken(":") or plx) and (pnChars or literalToken(":") or plx))
    val pnPrefix by pnCharsBase and optional(zeroOrMore(pnChars or dot) and pnChars)

    val prefixIdStart by literalToken("@prefix")
    val baseStart by literalToken("@base")
    val sparqlBaseStart by literalToken("BASE")
    val sparqlPrefixStart by literalToken("PREFIX")

    val prefixID by -prefixIdStart and pname_ns and iriref and -dot
    val base by -baseStart and iri and -dot
    val sparqlBase by -sparqlBaseStart and iri
    val sparqlPrefix by -sparqlPrefixStart and pname_ns and iri


    val prefixPars by oneOrMore(prefixID) map {
        buildMap {
            it.forEach { (prefix, uri) ->
                this[prefix.text] = uri
            }
        }
    }

    override val rootParser: Parser<Map<String, String>> by prefixPars
}