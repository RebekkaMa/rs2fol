import IRIConstants.RDF_TYPE_IRI
import N3sToFolParser.createBlankNodeId
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import rdfSurface.*
import rdfSurface.Collection

object RDFSurfacesParser : Grammar<PositiveRDFSurface>() {

    val lineComment by regexToken("^\\s*#.*$".toRegex(RegexOption.MULTILINE), ignore = true) //TODO("skolem")
    val inlineComment by regexToken("\\s#.*$".toRegex(RegexOption.MULTILINE), ignore = true) //TODO("skolem")

    val space by regexToken("\\s+", ignore = true)

    var blankNodeCounter = 0
    val blankNodeTriplesSet = mutableListOf<RdfTriple>()
    val prefixMap = mutableMapOf<String, String>()
    var baseIri: String? = null

    val varSet = mutableSetOf<BlankNode>()

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
    val sparqlBaseStart by literalToken("BASE") //TODO(case-insensitive)
    val sparqlPrefixStart by literalToken("PREFIX") //TODO(case-insensitive)

    val iriref by regexToken("<([^\u0000-\u0020<>\"{}|^`\\\\]|((\\\\u([0-9]|[A-F]|[a-f]){4})|(\\\\U([0-9]|[A-F]|[a-f]){8})))*>")

    val blankNodeLabelToken by regexToken("_:($pnCharsU|[0-9])(($pnChars|\\.)*$pnChars)?")

    val pnameLn by regexToken("$pnPrefix?:$pnLocal")
    val pnameNs by regexToken("$pnPrefix?:")
    val prefixedName by pnameLn.use {
        val (prefix, local) = this.text.split(':', limit = 2)
        return@use (prefixMap[prefix] ?: throw Exception("Prefix not found")) + local
    } or pnameNs.use {
        prefixMap[this.text.trimEnd().dropLast(1)] ?: throw Exception("Prefix not found")
    } map { IRI(it) }

    val iri by iriref.map {
        it.text.removeSurrounding("<", ">").let { rawIriMatch -> IRI((baseIri ?: "") + rawIriMatch) }
    } or prefixedName


    val blankNodeLabel by blankNodeLabelToken.use { BlankNode(this.text.drop(2)) }
    val anon by lparBracket and rparBracket
    val blankNode by blankNodeLabel or anon.use { BlankNode(createBlankNodeId()) }

    val numericLiteralToken by regexToken("(?<!:)\\b((([+-]?([0-9]+\\.[0-9]*([eE][+-]?[0-9]+)))|(\\.[0-9]+([eE][+-]?[0-9]+))|([0-9]+([eE][+-]?[0-9]+)))|([+-]?[0-9]*\\.[0-9]+)|([+-]?[0-9]+))\\b")
    val numericLiteral by numericLiteralToken use { Literal.fromNumericLiteral(this.text) }

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
    val rdfLiteral by string and optional(langTag or ((circumflex and iri) use {this.t2})) use {
        when (val part = this.t2) {
            null -> Literal.fromNonNumericLiteral(this.t1, dataTypeIRI = IRI(IRIConstants.XSD_STRING_IRI))
            is String -> Literal.fromNonNumericLiteral(this.t1, langTag = part)
            is IRI -> Literal.fromNonNumericLiteral(this.t1, dataTypeIRI = part)
            else -> throw Exception("Internal Error")
        }
    }
    val booleanLiteralToken by regexToken("(true)|(false)")
    val booleanLiteral by booleanLiteralToken use {
        Literal.fromNonNumericLiteral(
            this.text,
            dataTypeIRI = IRI(IRIConstants.XSD_BOOLEAN_IRI)
        )
    }

    val literal by numericLiteral or rdfLiteral or booleanLiteral

    val blankNodePropertyList: Parser<BlankNode> by -lparBracket and parser(this::predicateObjectList) and -rparBracket map { (pred, objList, semicolonRestList) ->
        val blankNodeSubj = BlankNode(createBlankNodeId())
        varSet.add(blankNodeSubj)
        objList.forEach {
            blankNodeTriplesSet.add(RdfTriple(blankNodeSubj, pred, it))
        }
        semicolonRestList.forEach {
            val (semiPred, semiObjList) = it ?: return@forEach
            semiObjList.forEach { semiObj ->
                blankNodeTriplesSet.add(RdfTriple(blankNodeSubj, semiPred, semiObj))
            }
        }
        blankNodeSubj
    }

    val collection: Parser<RdfTripleElement> by -lpar and zeroOrMore(parser(this::rdfObject)) and -rpar use {
        Collection(this)
    }

    val subject by iri or blankNode.map { varSet.add(it); it } or literal or collection
    val predicate by iri or blankNode.map { varSet.add(it); it } or literal
    val rdfObject: Parser<RdfTripleElement> by iri or blankNode.map { varSet.add(it); it } or literal or blankNodePropertyList or collection

    val verb by predicate or a.map { IRI(RDF_TYPE_IRI) }

    val objectList by rdfObject and zeroOrMore(-comma and rdfObject) use { listOf(this.t1).plus(this.t2) }
    val predicateObjectList by verb and objectList and zeroOrMore(
        -semicolon and optional(verb and objectList)
    )

    val prefixID by -prefixIdStart and pnameNs and iriref and -dot
    val base by -baseStart and iriref and -dot
    val sparqlBase by -sparqlBaseStart and iriref
    val sparqlPrefix by -sparqlPrefixStart and pnameNs and iriref


    val triples by (subject and predicateObjectList) or (blankNodePropertyList and optional(predicateObjectList)) map { (subj, predObjList) ->
        val triplesResult = buildList {
            if (predObjList != null) {
                val (pred, objList, semicolonRestList) = predObjList
                objList.forEach {
                    this.add(RdfTriple(subj, pred, it))
                }
                semicolonRestList.forEach {
                    val (semiPred, semiObjList) = it ?: return@forEach
                    semiObjList.forEach { semiObj ->
                        this.add(
                            RdfTriple(
                                subj,
                                semiPred,
                                semiObj
                            )
                        )
                    }
                }
            }
            if (blankNodeTriplesSet.isNotEmpty()) {
                this.addAll(blankNodeTriplesSet)
                blankNodeTriplesSet.clear()
            }
        }
        val freeVariables = varSet.toSet()
        varSet.clear()
        Pair(triplesResult, freeVariables)
    }

    val emptyvarList by (lpar and rpar) use { listOf<BlankNode>() }
    val variableList by emptyvarList or (-lpar and oneOrMore(blankNode) and -rpar)

    // Triple(hayesGraph, freeVariables, collectionBlankNodes)
    val rdfSurfacesParser: Parser<Pair<List<HayesGraphElement>, Set<BlankNode>>> by
    (variableList and
            (iri) and
            -lparcurl and
            (oneOrMore(parser(this::rdfSurfacesParser)) or optional(space).use {
                listOf()
            }) and
            -rparcurl) map { (variableList, surface, rest) ->
        val (hayeGraph, freeVariables) = rest.reduceOrNull { acc, pair ->
            Pair(acc.first.plus(pair.first), acc.second.plus(pair.second))
        } ?: Pair(listOf(), setOf())
        val variableListStrings = variableList
        val newHayeGraph = buildList<HayesGraphElement> {
            when (surface.iri) {
                IRIConstants.LOG_POSITIVE_SURFACE_IRI -> this.add(PositiveRDFSurface(variableListStrings, hayeGraph))
                IRIConstants.LOG_NEGATIVE_TRIPLE_IRI -> this.add(
                    NegativeTripleRDFSurface(
                        variableListStrings,
                        hayeGraph
                    )
                )
                IRIConstants.LOG_QUERY_SURFACE_IRI -> this.add(QueryRDFSurface(variableListStrings, hayeGraph))
                IRIConstants.LOG_NEGATIVE_SURFACE_IRI -> this.add(NegativeRDFSurface(variableListStrings, hayeGraph))
                IRIConstants.LOG_NEUTRAL_SURFACE_IRI -> this.add(NeutralRDFSurface(variableListStrings, hayeGraph))
                else -> throw Exception("Surface IRI not supported")
            }
        }
        Pair(newHayeGraph, freeVariables.minus(variableList.toSet()))
    } or triples and -optional(dot)

    val directive by (prefixID or sparqlPrefix).map {
        prefixMap.put(it.t1.text.dropLast(1), it.t2.text.removeSurrounding("<", ">"))
    } or (base or sparqlBase).map { baseIri = it.text.drop(1).dropLast(1) } use { null }

    val statement by directive or rdfSurfacesParser
    val turtleDoc by oneOrMore(statement) map {
        val (hayesGraph, freeVariables) = it.filterNotNull().reduceOrNull { acc, pair ->
            Pair(acc.first.plus(pair.first), acc.second.plus(pair.second))
        } ?: Pair(listOf(), setOf())
        if ((hayesGraph.size == 1) && (hayesGraph.first() is PositiveRDFSurface)) hayesGraph.first() as PositiveRDFSurface else PositiveRDFSurface(
            freeVariables.toList(),
            hayesGraph
        )
    }

    override val rootParser: Parser<PositiveRDFSurface> by turtleDoc use {
        varSet.clear()
        blankNodeTriplesSet.clear()
        prefixMap.clear()
        baseIri = null
        this
    }


    fun createBlankNodeId(): String {
        blankNodeCounter++
        return "BN_$blankNodeCounter"
    }

    //TODO()
    fun resetAll() {
        blankNodeCounter = 0
        varSet.clear()
        blankNodeTriplesSet.clear()
        prefixMap.clear()
        baseIri = null
        blankNodeTriplesSet.clear()
    }
}