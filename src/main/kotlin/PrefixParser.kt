import N3sToFolParser.hex
import N3sToFolParser.iri
import N3sToFolParser.unicodeBig
import N3sToFolParser.unicodeSmall
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object PrefixParser : Grammar<Map<String, String>>() {

    val space by regexToken("\\s+", ignore = true)

    val dot by literalToken(".")
    val semicolon by literalToken(";")
    val colon by literalToken(":")
    val underscore by literalToken("_")
    val minus by literalToken("-")
    val percentLetter by literalToken("%")

    val prefixIdStart by literalToken("@prefix")
    val baseStart by literalToken("@base")
    val sparqlBaseStart by literalToken("BASE")
    val sparqlPrefixStart by literalToken("PREFIX")

    val re by regexToken("(([A-Z]|[a-z]|[À-Ö]|[Ø-ö]|[ø-˿]|[Ͱ-ͽ]|[Ϳ-\u1FFF]|[\u200C-\u200D]|[⁰-\u218F]|[Ⰰ-\u2FEF]|[、-\uD7FF]|[豈-\uFDCF]|[ﷰ-�]|[\uD800\uDC00-\uDB7F\uDFFF])((((([A-Z]|[a-z]|[À-Ö]|[Ø-ö]|[ø-˿]|[Ͱ-ͽ]|[Ϳ-\u1FFF]|[\u200C-\u200D]|[⁰-\u218F]|[Ⰰ-\u2FEF]|[、-\uD7FF]|[豈-\uFDCF]|[ﷰ-�]|[\uD800\uDC00-\uDB7F\uDFFF])|_)|-|([0-9]|·|[̀-ͯ]|[‿-⁀]))|\\.)*((([A-Z]|[a-z]|[À-Ö]|[Ø-ö]|[ø-˿]|[Ͱ-ͽ]|[Ϳ-\u1FFF]|[\u200C-\u200D]|[⁰-\u218F]|[Ⰰ-\u2FEF]|[、-\uD7FF]|[豈-\uFDCF]|[ﷰ-�]|[\uD800\uDC00-\uDB7F\uDFFF])|_)|-|([0-9]|·|[̀-ͯ]|[‿-⁀])))?)?:")

    val pnCharsBase by regexToken("[A-Z]|[a-z]|[\u00C0-\u00D6]|[\u00D8-\u00F6]|[\u00F8-\u02FF]|[\u0370-\u037D]|[\u037F-\u1FFF]|[\u200C-\u200D]|[\u2070-\u218F]|[\u2C00-\u2FEF]|[\u3001-\uD7FF]|[\uF900-\uFDCF]|[\uFDF0-\uFFFD]|[\uD800\uDC00-\uDB7F\uDFFF]")
    val pnCharsU by pnCharsBase or underscore
    val pnCharPart by regexToken("[0-9]|\u00B7|[\u0300-\u036F]|[\u203F-\u2040]")
    val pnChars by pnCharsU or minus or pnCharPart
    val pnPrefix by pnCharsBase and re map { (pcb,pnch) -> pcb.text + pnch.text }
    val pname_ns by re use {this.text}

    val hex by regexToken("[0-9]|[A-F]|[a-f]")
    val UCHAR by (unicodeSmall and hex and hex and hex and hex).use { this.t1.text + this.t2.text + this.t3.text + this.t4.text + this.t5.text} or (unicodeBig and hex and hex and hex and hex and hex and hex and hex and hex).use { this.t1.text + this.t2.text + this.t3.text + this.t4.text + this.t5.text + this.t6.text + this.t7.text + this.t8.text + this.t9.text}
    val irireftext by regexToken("[^\u0000-\u0020<>\"{}|^`]")
    val iriref by literalToken("<") and zeroOrMore(irireftext.use { this.text } or UCHAR) and literalToken(">") use {this.t1.text + this.t2.joinToString(separator = "") +this.t3.text}
    //val iri by regexToken("<[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*>")

    val pnLocalESC by regexToken("\\\\[-_~.!$&'()*+,;=/?#@%]")
    val percent by percentLetter and hex and hex
    val plx by percent or pnLocalESC
    val pnLocal by (pnCharsU or colon or regexToken("[0-9]") or plx) and optional(zeroOrMore(pnChars or dot or colon or plx) and (pnChars or literalToken(":") or plx))




    val prefixID by -prefixIdStart and pname_ns and iriref and -dot
    //val base by -baseStart and iri and -dot
    //val sparqlBase by -sparqlBaseStart and iri
    //val sparqlPrefix by -sparqlPrefixStart and pname_ns and iri


    val prefixPars by oneOrMore(prefixID) map {
        buildMap {
            it.forEach { (prefix, uri) ->
                this[prefix] = uri
            }
        }
    }

    override val rootParser: Parser<Map<String, String>> by prefixPars
}