import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object N3sToFolParser : Grammar<String?>() {

    var blankNodeCounter = 0
    var blankNodeTriplesSet = mutableListOf<String>()
    var prefixMap = mapOf<String, String>()

    val varSet = mutableSetOf<String>()

    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val lparcurl by literalToken("{")
    val rparcurl by literalToken("}")
    val lparBracket by literalToken("[")
    val rparBracket by literalToken("]")
    val space by regexToken("\\s+", ignore = true)
    val dot by literalToken(".")
    val semicolon by literalToken(";")
    val comma by literalToken(",")
    val a by literalToken("a")
    // val comment by regexToken("^#") //TODO("skolem")

    val negativeSurfaceIRI by literalToken("<http://www.w3.org/2000/10/swap/log#onNegativeSurface>")
    val positiveSurfaceIRI by literalToken("<http://www.w3.org/2000/10/swap/log#onPositiveSurface>")
    val querySurfaceIRI by literalToken("<http://www.w3.org/2000/10/swap/log#onQuerySurface>")
    val neutralSurfaceIRI by literalToken("<http://www.w3.org/2000/10/swap/log#onNeutralSurface>")

    val pnLocalESC = "(\\\\[-_~.!$&'()*+,;=/?#@%])".toRegex()
    val hex = "([0-9]|[A-F]|[a-f])".toRegex()
    val percent = "(%$hex{2})".toRegex()
    val plx = "$percent|$pnLocalESC".toRegex()

    val pnCharsBase = "([A-Z]|[a-z]|[\u00C0-\u00D6]|[\u00D8-\u00F6]|[\u00F8-\u02FF]|[\u0370-\u037D]|[\u037F-\u1FFF]|[\u200C-\u200D]|[\u2070-\u218F]|[\u2C00-\u2FEF]|[\u3001-\uD7FF]|[\uF900-\uFDCF]|[\uFDF0-\uFFFD]|[\uD800\uDC00-\uDB7F\uDFFF])".toRegex()
    val pnCharsU = "($pnCharsBase|_)".toRegex()
    val pnChars = "($pnCharsU|-|[0-9]|\u00B7|[\u0300-\u036F]|[\u203F-\u2040])".toRegex()

    val pnLocal = "(($pnCharsU|:|[0-9]|$plx)(($pnChars|\\.|:|$plx)*($pnChars|:|$plx))?)".toRegex()
    val pnPrefix = "($pnCharsBase(($pnChars|\\.)*$pnChars)?)".toRegex()

    val iriref by regexToken("<([^\u0000-\u0020<>\"{}|^`\\\\]|((\\\\u([0-9]|[A-F]|[a-f]){4})|(\\\\U([0-9]|[A-F]|[a-f]){8})))*>")

    val pnameNs by regexToken("$pnPrefix?:")
    val pnameLn by regexToken("$pnameNs$pnLocal")
    val prefixedName by pnameLn or pnameNs

    val echar = "(\\[tbnrf\"'\\\\])".toRegex()
    val uchar = "((\\\\u$hex{4})|(\\\\U$hex{8}))".toRegex()

    val iri by iriref or prefixedName use {"'" + this.text + "'"}

    val blankNodeLabelToken by regexToken("_:($pnCharsU|[0-9])(($pnChars|\\.)*$pnChars)?")
    val blankNodeLabel by blankNodeLabelToken.use { this.text.drop(2).replaceFirstChar { it.uppercaseChar() } }
    val anon by lparBracket and rparBracket //TODO(WS*)
    val blankNode by blankNodeLabel or anon.use { createBlankNodeId() }

    val stringLiteralQuote  = "(\"([^\u0022\u000A\u000D\\\\]|$echar|$uchar)*\")".toRegex()
    val stringLiteralSingleQuote = "('([^\u0022\u000A\u000D\\\\]|$echar|$uchar)*')".toRegex()
    val stringLiteralLongSingleQuote = "('''(('|(''))?([^'\\\\]|$echar|$uchar))*''')".toRegex()
    val stringLiteralLongQuote = "(\"\"\"((\"|(\"\"))?([^\"\\\\]|$echar|$uchar))*\"\"\")".toRegex()


    val stringToken by regexToken("$stringLiteralQuote|$stringLiteralSingleQuote|$stringLiteralLongSingleQuote|$stringLiteralLongQuote")
    val string by stringToken use { this.text }
    val langTagToken by regexToken("@[a-zA-Z]+(-[a-zA-Z0-9]+)*")
    val langTag by langTagToken use { this.text }
    val circumflexToken by literalToken("^^")
    val circumflex by circumflexToken use { this.text }
    val rdfLiteral by string and optional(langTag or ((circumflex and iri) use { this.t1 + this.t2 })) use { "'" + this.t1 + (if (this.t2.isNullOrEmpty()) "" else this.t2) + "'" }
    val numericLiteralToken by regexToken("(([+-]?([0-9]+\\.[0-9]*([eE][+-]?[0-9]+)))|(\\.[0-9]+([eE][+-]?[0-9]+))|([0-9]+([eE][+-]?[0-9]+)))|([+-]?[0-9]*\\.[0-9]+)|([+-]?[0-9]+)")
    val numericLiteral by numericLiteralToken use { "'" + this.text + "'" }
    val booleanLiteralToken by regexToken("(true)|(false)")
    val booleanLiteral by booleanLiteralToken use { this.text }

    val literal by rdfLiteral or numericLiteral or booleanLiteral

    val blankNodePropertyList : Parser<String> by -lparBracket and parser(this::predicateObjectList) and -rparBracket map { (pred, objList, semicolonRestList) ->
        val blankNodeSubj = createBlankNodeId()
        varSet.add(blankNodeSubj)
        val blankNodeTriples = buildString{
            this.append(objList.joinToString(separator = " & ") {
                createTripleString(
                    str1 = blankNodeSubj,
                    str2 = pred,
                    str3 = it
                )
            })
            semicolonRestList.forEach {
                val (semiPred, semiObjList) = it ?: return@forEach
                this.append(semiObjList.joinToString(prefix = " & ", separator = " & ") { semiObj ->
                    createTripleString(
                        str1 = blankNodeSubj,
                        str2 = semiPred,
                        str3 = semiObj
                    )
                })
            }
        }
        blankNodeTriplesSet.add(blankNodeTriples)
        blankNodeSubj
    }

    val collection : Parser<String> by -lpar and zeroOrMore(parser(this::rdfObject)) and -rpar use {"list("+this.joinToString(",")+")"}

    val subject by iri or blankNode.map { varSet.add(it); it } or literal or collection
    val predicate by iri or blankNode.map { varSet.add(it); it } or literal
    val rdfObject : Parser<String> by iri or blankNode.map { varSet.add(it); it } or literal or blankNodePropertyList or collection

    val verb by predicate or a.map { "'<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>'" }

    val objectList by rdfObject and zeroOrMore(-comma and rdfObject) use { listOf(this.t1).plus(this.t2) }
    val predicateObjectList  by verb and objectList and zeroOrMore(
        -semicolon and optional(verb and objectList)
    )


    val triples by (subject and predicateObjectList) or (blankNodePropertyList and predicateObjectList) map { (subj, predObjList) -> //TODO("0-1 times predicateObjectList")
        val (pred, objList, semicolonRestList) = predObjList
        buildString {
            this.append(objList.joinToString(separator = " & ") {
                createTripleString(
                    str1 = subj,
                    str2 = pred,
                    str3 = it
                )
            })
            semicolonRestList.forEach {
                val (semiPred, semiObjList) = it ?: return@forEach
                this.append(semiObjList.joinToString(prefix = " & ", separator = " & ") { semiObj ->
                    createTripleString(
                        str1 = subj,
                        str2 = semiPred,
                        str3 = semiObj
                    )
                })
            }
            if (blankNodeTriplesSet.isNotEmpty()) this.append(blankNodeTriplesSet.joinToString(prefix = " & ", separator = " & "))
        }
    }

    val emptyvarList by (lpar and rpar) use { listOf<String>() }
    val variableList by emptyvarList or (-lpar and oneOrMore(blankNode) and -rpar)

    val N3sToFolParserNonDefault: Parser<String> by (oneOrMore(
        (variableList and
                (negativeSurfaceIRI or positiveSurfaceIRI or querySurfaceIRI) and
                -lparcurl and
                (parser(this::N3sToFolParserNonDefault) or optional(space).use { "\$true" }) and
                -rparcurl) map { (variableList, surface, rest) ->
            val variableListNotNull = variableList.isNotEmpty()
            val variableListStrings = variableList.joinToString(separator = ",")
            varSet.removeAll(variableList.toSet())
            buildString {
                when(surface.type){
                    positiveSurfaceIRI -> {
                        if (variableListNotNull) {
                            this.append("? [$variableListStrings] : ")
                        }
                    }
                    else -> {
                        if (variableListNotNull) {
                            this.append("! [$variableListStrings] : ")
                        }
                        this.append("~")
                    }
                }
                this.append(rest)
            }
        } or triples
                and -optional(dot)) use { this.joinToString(prefix = "(", postfix = ")", separator = " & ") })

    val N3sToFolParser: Parser<String> by
    (oneOrMore(
        (variableList and
                (negativeSurfaceIRI or positiveSurfaceIRI or querySurfaceIRI) and
                -lparcurl and
                (parser(this::N3sToFolParserNonDefault) or optional(space).use { "\$true" }) and
                -rparcurl) map { (variableList, surface, rest) ->
            val variableListNotNull = variableList.isNotEmpty()
            val variableListStrings = variableList.joinToString(separator = ",")
            varSet.removeAll(variableList.toSet())
            buildString {
                when (surface.type){
                    positiveSurfaceIRI -> {
                        if (variableListNotNull) {
                            this.append("? [$variableListStrings] : ")
                        }
                        this.append(rest)
                    }
                    negativeSurfaceIRI -> {
                        if (variableListNotNull) {
                            this.append("! [$variableListStrings] : ")
                        }
                        this.append("~")
                        this.append(rest)
                    }
                    else -> {}
                }
            }
        } or triples
                and -optional(dot)) use { this.filter { it.isNotEmpty() }.joinToString(prefix = "(", postfix = ")", separator = " & ") })


    val parserWithPrefix by PrefixParser.map { prefixMap = it } and N3sToFolParser.use {
        if (varSet.isEmpty()) this else {
            val varSetString = varSet.joinToString(separator = ",")
            varSet.clear()
            " ? [$varSetString] : $this"
        }
    }

    override val rootParser: Parser<String> by N3sToFolParser use {
        if (varSet.isEmpty()) this else {
            val varSetString = varSet.joinToString(separator = ",")
            varSet.clear()
            " ? [$varSetString] : $this"
        }
    }

    fun createFofAnnotatedAxiom(formula: String?) = "fof(axiom,axiom,$formula)."
    fun createFofAnnotatedConjecture(formula: String?) = "fof(conjecture,conjecture,$formula)."

    fun createBlankNodeId(): String {
        blankNodeCounter++
        return "BN_$blankNodeCounter"
    }

    private fun createTripleString(str1: String, str2: String, str3: String): String = "triple($str1,$str2,$str3)"
}