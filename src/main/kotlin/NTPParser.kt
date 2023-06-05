import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object N3sToFolParser : Grammar<String?>() {

    val lineComment by regexToken("^\\s*#.*$".toRegex(RegexOption.MULTILINE), ignore = true) //TODO("skolem")
    val inlineComment by regexToken("\\s#.*$".toRegex(RegexOption.MULTILINE), ignore = true) //TODO("skolem")

    val transformer = RDFLiteralToTPTPConstantTransformer()

    val space by regexToken("\\s+", ignore = true)

    var blankNodeCounter = 0
    var blankNodeTriplesSet = mutableListOf<String>()
    val prefixMap = mutableMapOf<String, String>()
    var baseIri: String? = null

    val varSet = mutableSetOf<String>()

    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val lparcurl by literalToken("{")
    val rparcurl by literalToken("}")
    val lparBracket by literalToken("[")
    val rparBracket by literalToken("]")
    val dot by literalToken(".")
    val semicolon by literalToken(";")
    val comma by literalToken(",")
    val a by regexToken("\\ba\\b")

    val prefixIdStart by literalToken("@prefix")
    val baseStart by literalToken("@base")
    val sparqlBaseStart by literalToken("BASE")
    val sparqlPrefixStart by literalToken("PREFIX")


    val negativeSurfaceIRI by literalToken("log:onNegativeSurface")
    val positiveSurfaceIRI by literalToken("log:onPositiveSurface")
    val querySurfaceIRI by literalToken("log:onQuerySurface")
    val neutralSurfaceIRI by literalToken("log:onNeutralSurface")

    val iriref by regexToken("<([^\u0000-\u0020<>\"{}|^`\\\\]|((\\\\u([0-9]|[A-F]|[a-f]){4})|(\\\\U([0-9]|[A-F]|[a-f]){8})))*>")

    val blankNodeLabelToken by regexToken("_:($pnCharsU|[0-9])(($pnChars|\\.)*$pnChars)?")

    val pnameLn by regexToken("$pnPrefix?:$pnLocal")
    val pnameNs by regexToken("$pnPrefix?:")
    val prefixedName by pnameLn.use {
        val (prefix, local) = this.text.split(':', limit = 2)
        return@use prefixMap[prefix] + local
    } or pnameNs.use { prefixMap[this.text.trimEnd().dropLast(1)] }

    val iri by iriref.map {
        if (baseIri != null) "$baseIri" + it.text.removeSurrounding(
            "<",
            ">"
        ) else it.text.removeSurrounding("<", ">")
    } or prefixedName


    val blankNodeLabel by blankNodeLabelToken.use { this.text.drop(2).replaceFirstChar { it.uppercaseChar() } }
    val anon by lparBracket and rparBracket //TODO(WS*)
    val blankNode by blankNodeLabel or anon.use { createBlankNodeId() }

    val numericLiteralToken by regexToken("(?<!:)\\b((([+-]?([0-9]+\\.[0-9]*([eE][+-]?[0-9]+)))|(\\.[0-9]+([eE][+-]?[0-9]+))|([0-9]+([eE][+-]?[0-9]+)))|([+-]?[0-9]*\\.[0-9]+)|([+-]?[0-9]+))\\b")
    val numericLiteral by numericLiteralToken use { transformer.transformNumericLiteral(this.text) }

    val stringLiteralLongSingleQuote by regexToken("('''(('|(''))?([^'\\\\]|$echar|$uchar))*''')")
    val stringLiteralLongQuote by regexToken("(\"\"\"((\"|(\"\"))?([^\"\\\\]|$echar|$uchar))*\"\"\")")
    val stringLiteralQuote by regexToken("(\"([^\u0022\u000A\u000D\\\\]|$echar|$uchar)*\")")
    val stringLiteralSingleQuote by regexToken("('([^\u0022\u000A\u000D\\\\]|$echar|$uchar)*')")

    val string by stringLiteralLongSingleQuote.use { this.text.removeSurrounding("'''") } or stringLiteralLongQuote.use {
        this.text.removeSurrounding(
            "\"\"\""
        )
    } or stringLiteralQuote.use { this.text.removeSurrounding("\"") } or stringLiteralSingleQuote.use {
        this.text.removeSurrounding(
            "'"
        )
    }

    val langTagToken by regexToken("@[a-zA-Z]+(-[a-zA-Z0-9]+)*")
    val langTag by langTagToken use { this.text }
    val circumflexToken by literalToken("^^")
    val circumflex by circumflexToken use { this.text }
    val rdfLiteral by string and optional(langTag or ((circumflex and iri) use { this.t2 })) use {
        if (this.t2.isNullOrEmpty()) transformer.transformLexicalValue(
            this.t1
        ) else transformer.transformLexicalValue(this.t1, this.t2!!)
    }
    val booleanLiteralToken by regexToken("(true)|(false)")
    val booleanLiteral by booleanLiteralToken use {
        transformer.transformLexicalValue(
            this.text,
            transformer.xsdIri + "boolean"
        )
    }

    val literal by numericLiteral or rdfLiteral or booleanLiteral

    val blankNodePropertyList: Parser<String> by -lparBracket and parser(this::predicateObjectList) and -rparBracket map { (pred, objList, semicolonRestList) ->
        val blankNodeSubj = createBlankNodeId()
        varSet.add(blankNodeSubj)
        val blankNodeTriples = buildString {
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

    val collection: Parser<String> by -lpar and zeroOrMore(parser(this::rdfObject)) and -rpar use {
        val listSize = this.size
        if (listSize == 0) "list0" else "list$listSize(" + this.joinToString(",") + ")"
    }

    val subject by iri use { "'$this'" } or blankNode.map { varSet.add(it); it } or literal or collection
    val predicate by iri use { "'$this'" } or blankNode.map { varSet.add(it); it } or literal
    val rdfObject: Parser<String> by iri use { "'$this'" } or blankNode.map { varSet.add(it); it } or literal or blankNodePropertyList or collection

    val verb by predicate or a.map { "'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'" }

    val objectList by rdfObject and zeroOrMore(-comma and rdfObject) use { listOf(this.t1).plus(this.t2) }
    val predicateObjectList by verb and objectList and zeroOrMore(
        -semicolon and optional(verb and objectList)
    )

    val prefixID by -prefixIdStart and pnameNs and iriref and -dot
    val base by -baseStart and iriref and -dot
    val sparqlBase by -sparqlBaseStart and iriref
    val sparqlPrefix by -sparqlPrefixStart and pnameNs and iriref


    val triples by (subject and predicateObjectList) or (blankNodePropertyList and predicateObjectList) map { (subj, predObjList) -> //TODO("0-1 times predicateObjectList")
        val (pred, objList, semicolonRestList) = predObjList
        val triplesResult = buildString {
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
            if (blankNodeTriplesSet.isNotEmpty()) {
                this.append(
                    blankNodeTriplesSet.joinToString(
                        prefix = " & ",
                        separator = " & "
                    )
                )
                blankNodeTriplesSet.clear()
            }
        } to varSet.toSet()
        varSet.clear()
        triplesResult
    }

    val emptyvarList by (lpar and rpar) use { listOf<String>() }
    val variableList by emptyvarList or (-lpar and oneOrMore(blankNode) and -rpar)

    val N3sToFolParserNonDefault: Parser<Pair<String,Set<String>>> by (oneOrMore(
        (variableList and
                (negativeSurfaceIRI or positiveSurfaceIRI or querySurfaceIRI) and
                -lparcurl and
                (parser(this::N3sToFolParserNonDefault) or optional(space).use { "\$true" to setOf<String>() }) and
                -rparcurl) map { (variableList, surface, rest) ->
            val (restString, freeVariables) = rest
            val variableListIsNotNull = variableList.isNotEmpty()
            val variableListStrings = variableList.joinToString(separator = ",")
            varSet.removeAll(variableList.toSet())
            buildString {
                when (surface.type) {
                    positiveSurfaceIRI -> {
                        if (variableListIsNotNull) {
                            this.append("? [$variableListStrings] : ")
                        }
                    }

                    else -> {
                        if (variableListIsNotNull) {
                            this.append("! [$variableListStrings] : ")
                        }
                        this.append("~")
                    }
                }
                this.append(restString)
            } to freeVariables.minus(variableList.toSet())
        } or triples
                and -optional(dot)) use {
                    val listSize = this.size
                    this.foldIndexed("(" to setOf<String>()){
                        index, acc, pair ->
                        if (index < listSize - 1){
                            acc.first + pair.first + " & "
                        } else {
                            acc.first + pair.first + ")"
                        } to acc.second.plus(pair.second)
                    }
                })

    val N3sToFolParser: Parser<Pair<String,Set<String>>> by
    (variableList and
            (negativeSurfaceIRI or positiveSurfaceIRI or querySurfaceIRI) and
            -lparcurl and
            (parser(this::N3sToFolParserNonDefault) or optional(space).use { "\$true" to setOf<String>() }) and
            -rparcurl) map { (variableList, surface, rest) ->
                val (restString, freeVariables) = rest
        val variableListNotNull = variableList.isNotEmpty()
        val variableListStrings = variableList.joinToString(separator = ",")
        buildString {
            when (surface.type) {
                positiveSurfaceIRI -> {
                    if (variableListNotNull) {
                        this.append("? [$variableListStrings] : ")
                    }
                    this.append(restString)
                }
                negativeSurfaceIRI -> {
                    if (variableListNotNull) {
                        this.append("! [$variableListStrings] : ")
                    }
                    this.append("~")
                    this.append(restString)
                }
                else -> {}
            }
        } to freeVariables.minus(variableList.toSet())
    } or triples and -optional(dot)

    val directive by (prefixID or sparqlPrefix).map {
        prefixMap.put(it.t1.text.dropLast(1), it.t2.text.removeSurrounding("<", ">"))
    } or (base or sparqlBase).map { baseIri = it.text.drop(1).dropLast(1) } use { null }

    val statement by directive or N3sToFolParser
    val turtleDoc by oneOrMore(statement) use {
        this.filter { (it != null) and (it?.first?.isNotBlank() ?: false) } .let {
            if (it.isEmpty()) return@let "\$true" to setOf()
            val listSize = it.size
            return@let it.foldIndexed("(" to setOf<String>()){
                    index, acc, pair ->
                if (index < listSize - 1){
                    acc.first + (pair?.first ?: "") + " & "
                } else {
                    acc.first + (pair?.first ?: "") + ")"
                } to acc.second.plus(pair?.second ?: setOf())
            }

        }
    }

    override val rootParser: Parser<String> by turtleDoc use {
        varSet.clear()
        blankNodeTriplesSet.clear()
        prefixMap.clear()
        baseIri = null
        val (triplesString, freeVariables) = this
        if (freeVariables.isEmpty()) triplesString else {
            val varSetString = freeVariables.joinToString(separator = ",")
            " ? [$varSetString] : $triplesString"
        }
    }

    fun createFofAnnotatedAxiom(formula: String?) = "fof(axiom,axiom,$formula)."
    fun createFofAnnotatedConjecture(formula: String?) = "fof(conjecture,conjecture,$formula)."

    fun createBlankNodeId(): String {
        blankNodeCounter++
        return "BN_$blankNodeCounter"
    }

    private fun createTripleString(str1: String, str2: String, str3: String): String = "triple($str1,$str2,$str3)"

    fun resetBlankNodeCounter(){
        blankNodeCounter=0
    }
}