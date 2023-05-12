import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object N3sToFolParser : Grammar<String?>() {

    val varSet = mutableSetOf<String>()

    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val lparcurl by literalToken("{")
    val rparcurl by literalToken("}")
    val space by regexToken("\\s+", ignore = true)
    val dot by literalToken(".")
    val semicolon by literalToken(";")
    val comma by literalToken(",")

    val negativeSurfaceIRI by literalToken("<http://www.w3.org/2000/10/swap/log#onNegativeSurface>")
    val positiveSurfaceIRI by literalToken("<http://www.w3.org/2000/10/swap/log#onPositiveSurface>")

    val blankNodeToken by regexToken("_:\\w+")
    val blankNode by blankNodeToken use { this.text.drop(2).replaceFirstChar { it.uppercaseChar() } }

    val iriToken by regexToken("<\\w*:[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*>")
    val iri by iriToken use { "'" + this.text + "'" }

    val stringToken by regexToken("('|\")\\w*('|\")")
    val string by stringToken use { this.text }
    val langTagToken by regexToken("@[a-zA-Z]+(-[a-zA-Z0-9]+)*")
    val langTag by langTagToken use { this.text }
    val circumflexToken by literalToken("^^")
    val circumflex by circumflexToken use { this.text }
    val rdfLiteral by string and optional(langTag or ((circumflex and iri) use { this.t1 + this.t2 })) use { this.t1 + this.t2 }
    val numericLiteralToken by regexToken("([+-]?[0-9]+)|([+-]?[0-9]*\\.[0-9]+)|([+-]?([0-9]+\\.[0-9]*([eE][+-]?[0-9]+)|\\.[0-9]+([eE][+-]?[0-9]+)|[0-9]+([eE][+-]?[0-9]+)))")
    val numericLiteral by numericLiteralToken use { this.text }
    val booleanLiteralToken by regexToken("(true)|(false)")
    val booleanLiteral by booleanLiteralToken use { this.text }

    val literal by rdfLiteral or numericLiteral or booleanLiteral

    val subject by iri or blankNode.map { varSet.add(it); it} or literal//TODO("or collection")
    val predicate by iri or blankNode.map { varSet.add(it); it} or literal
    val rdfObject by iri or blankNode.map { varSet.add(it); it} or literal //TODO("or collection or blankNodePropertyList")

    val objectList by rdfObject and zeroOrMore(-comma and rdfObject) use { listOf(this.t1).plus(this.t2) }
    val predicateObjectList by predicate and objectList and zeroOrMore(
        -semicolon and optional(predicate and objectList)
    )
    val triples by subject and predicateObjectList map { (subj, predObjList) ->
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
        }
    }

    val emptyvarList by (lpar and rpar) use { listOf<String>() }
    val variableList by emptyvarList or (-lpar and oneOrMore(blankNode) and -rpar)

    val N3sToFolParser: Parser<String> by
    (oneOrMore(
        (variableList and
                (negativeSurfaceIRI or positiveSurfaceIRI) and
                -lparcurl and
                (parser(this::N3sToFolParser) or optional(space).use { "\$true" }) and
                -rparcurl) map { (variableList, surface, rest) ->
            val variableListNotNull = variableList.isNotEmpty()
            val variableListStrings = variableList.joinToString(separator = ",")
            varSet.removeAll(variableList.toSet())
            buildString {
                if (surface.type == positiveSurfaceIRI) {
                    if (variableListNotNull) {
                        this.append("? [$variableListStrings] : ")
                    }
                } else {
                    if (variableListNotNull) {
                        this.append("! [$variableListStrings] : ")
                    }
                    this.append("~")
                }
                this.append(rest)
            }
        } or triples
                and -optional(dot)) use { this.joinToString(prefix = "(", postfix = ")", separator = " & ") })

    override val rootParser: Parser<String> by N3sToFolParser use {
        if (varSet.isEmpty()) this else {
            val varSetString = varSet.joinToString(separator = ",")
            varSet.clear()
            " ? [$varSetString] : $this"
        }}

    fun createFofAnnotatedAxiom(formula : String?) = "fof(axiom,axiom,$formula)."
    fun createFofAnnotatedConjecture(formula : String?) = "fof(conjecture,conjecture,$formula)."


    private fun createTripleString(str1: String, str2: String, str3: String): String = "triple($str1,$str2,$str3)"
}