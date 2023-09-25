package parser

import IRIConstants
import IRIConstants.RDF_TYPE_IRI
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import rdfSurfaces.*
import rdfSurfaces.Collection
import util.RDFSurfacesParseException

class RDFSurfacesParser(val useRDFLists: Boolean) : Grammar<PositiveSurface>() {

    val lineComment by regexToken("^\\s*#.*$".toRegex(RegexOption.MULTILINE), ignore = true)
    val inlineComment by regexToken("\\s#.*$".toRegex(RegexOption.MULTILINE), ignore = true)

    private lateinit var bnLabel: String

    private val space by regexToken("\\s+", ignore = true)

    private var blankNodeCounter = 0
    private val blankNodeTriplesSet = mutableListOf<RdfTriple>()
    private val prefixMap = mutableMapOf<String, String>()
    private var baseIri: IRI = IRI.from("") //TODO()

    private val collectionTripleSet = mutableListOf<RdfTriple>()
    private val blankNodeToDirectParentSet = mutableSetOf<BlankNode>()

    private val varSet = mutableSetOf<BlankNode>()

    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val lparcurl by literalToken("{")
    private val rparcurl by literalToken("}")
    private val lparBracket by literalToken("[")
    private val rparBracket by literalToken("]")
    private val dot by literalToken(".")
    private val semicolon by literalToken(";")
    private val comma by literalToken(",")
    private val a by regexToken("\\ba\\b")

    private val prefixIdStart by literalToken("@prefix")
    private val baseStart by literalToken("@base")
    private val sparqlBaseStart by regexToken("\\bBASE\\b".toRegex(RegexOption.IGNORE_CASE))
    private val sparqlPrefixStart by regexToken("\\bPREFIX\\b".toRegex(RegexOption.IGNORE_CASE))

    private val irirefToken by regexToken("<([^\u0000-\u0020<>\"{}|^`\\\\]|((\\\\u([0-9]|[A-F]|[a-f]){4})|(\\\\U([0-9]|[A-F]|[a-f]){8})))*>")
    private val iriref by irirefToken use { replaceNumericEscapeSequences(text.removeSurrounding("<", ">")) }

    private val blankNodeLabelToken by regexToken("_:($pnCharsU|[0-9])(($pnChars|\\.)*$pnChars)?")

    private val pnameLn by regexToken("$pnPrefix?:$pnLocal")
    private val pnameNs by regexToken("$pnPrefix?:")
    private val prefixedName by pnameLn.use {
        val (prefix, local) = text.split(':', limit = 2)
        return@use (prefixMap[prefix]
            ?: throw RDFSurfacesParseException("Undefined Prefix")) + replaceReservedCharacterEscapes(local)
    } or pnameNs.use {
        prefixMap[text.trimEnd().dropLast(1)] ?: throw RDFSurfacesParseException("Undefined Prefix")
    } map { IRI.from(it) }

    private val iri by iriref.map { iriString ->
        IRI.from(iriString).let {
            it.takeUnless { it.isRelativeReference() } ?: IRI.transformReference(R = it, B = baseIri)
        }
    } or prefixedName


    private val blankNodeLabel by blankNodeLabelToken.use { BlankNode(this.text.drop(2)) }
    private val anon by lparBracket and rparBracket
    private val blankNode by blankNodeLabel or anon.use { BlankNode(createBlankNodeId()) }

    private val numericLiteralToken by regexToken("(?<!:)\\b((([+-]?([0-9]+\\.[0-9]*([eE][+-]?[0-9]+)))|(\\.[0-9]+([eE][+-]?[0-9]+))|([0-9]+([eE][+-]?[0-9]+)))|([+-]?[0-9]*\\.[0-9]+)|([+-]?[0-9]+))\\b")
    private val numericLiteral by numericLiteralToken use { Literal.fromNumericLiteral(this.text) }

    private val stringLiteralLongSingleQuote by regexToken("('''(('|(''))?([^'\\\\]|$echar|$uchar))*''')")
    private val stringLiteralLongQuote by regexToken("(\"\"\"((\"|(\"\"))?([^\"\\\\]|$echar|$uchar))*\"\"\")")
    private val stringLiteralQuote by regexToken("(\"([^\u0022\u000A\u000D\\\\]|$echar|$uchar)*\")")
    private val stringLiteralSingleQuote by regexToken("('([^\u0022\u000A\u000D\\\\]|$echar|$uchar)*')")

    private val string by stringLiteralLongSingleQuote.use { text.removeSurrounding("'''") } or
            stringLiteralLongQuote.use { text.removeSurrounding("\"\"\"") } or
            stringLiteralQuote.use { text.removeSurrounding("\"") } or
            stringLiteralSingleQuote.use { text.removeSurrounding("'") }

    private val langTagToken by regexToken("@[a-zA-Z]+(-[a-zA-Z0-9]+)*")
    private val langTag by langTagToken use { text }
    private val circumflexToken by literalToken("^^")
    private val circumflex by circumflexToken use { text }
    private val rdfLiteral by string and optional(langTag or ((circumflex and iri) use { t2 })) use {
        when (val part = this.t2) {
            null -> Literal.fromNonNumericLiteral(
                replaceStringEscapes(replaceNumericEscapeSequences(this.t1)),
                datatypeIRI = IRI.from(IRIConstants.XSD_STRING_IRI)
            )

            is String -> Literal.fromNonNumericLiteral(this.t1, langTag = part.drop(1))
            is IRI -> {
                val literalValue = this.t1.takeUnless { part == IRIConstants.XSD_STRING_IRI } ?: replaceStringEscapes(
                    replaceNumericEscapeSequences(this.t1)
                )
                Literal.fromNonNumericLiteral(literalValue, datatypeIRI = part)
            }

            else -> throw RDFSurfacesParseException("RDF Literal type not supported")
        }
    }
    private val booleanLiteralToken by regexToken("(true)|(false)")
    private val booleanLiteral by booleanLiteralToken use {
        Literal.fromNonNumericLiteral(
            text,
            datatypeIRI = IRI.from(IRIConstants.XSD_BOOLEAN_IRI)
        )
    }

    private val literal by numericLiteral or rdfLiteral or booleanLiteral

    private val blankNodePropertyList: Parser<BlankNode> by -lparBracket and parser(this::predicateObjectList) and -rparBracket map { (pred, objList, semicolonRestList) ->
        BlankNode(createBlankNodeId()).also { blankNodeSubj ->
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
        }
    }

    private val collection: Parser<RdfTripleElement> by -lpar and zeroOrMore(parser(this::rdfObject)) and -rpar use {
        if (useRDFLists) {
            if (this.isEmpty()) IRI.from(IRIConstants.RDF_NIL_IRI) else {
                val collectionStart = BlankNode(createBlankNodeId())
                blankNodeToDirectParentSet.add(collectionStart)
                this.foldIndexed(collectionStart) { index, acc, s ->
                    collectionTripleSet.add(RdfTriple(acc, IRI.from(IRIConstants.RDF_FIRST_IRI), s))
                    if (index >= this.lastIndex) {
                        collectionTripleSet.add(
                            RdfTriple(
                                acc,
                                IRI.from(IRIConstants.RDF_REST_IRI),
                                IRI.from(IRIConstants.RDF_NIL_IRI)
                            )
                        )
                        return@foldIndexed collectionStart
                    }
                    val nextRestBlankNode = BlankNode(createBlankNodeId())
                    blankNodeToDirectParentSet.add(nextRestBlankNode)
                    collectionTripleSet.add(
                        RdfTriple(
                            acc,
                            IRI.from(IRIConstants.RDF_REST_IRI),
                            nextRestBlankNode
                        )
                    )
                    return@foldIndexed nextRestBlankNode
                }
            }
        } else (Collection(this))
    }

    private val subject by iri or blankNode.map { varSet.add(it); it } or literal or collection
    private val predicate by iri or blankNode.map { varSet.add(it); it } or literal
    private val rdfObject: Parser<RdfTripleElement> by iri or blankNode.map { varSet.add(it); it } or literal or blankNodePropertyList or collection

    private val verb by predicate or a.map { IRI.from(RDF_TYPE_IRI) }

    private val objectList by rdfObject and zeroOrMore(-comma and rdfObject) use { listOf(this.t1).plus(this.t2) }
    private val predicateObjectList by verb and objectList and zeroOrMore(
        -semicolon and optional(verb and objectList)
    )

    private val prefixID by -prefixIdStart and pnameNs and iriref and -dot
    private val base by -baseStart and iriref and -dot
    private val sparqlBase by -sparqlBaseStart and iriref
    private val sparqlPrefix by -sparqlPrefixStart and pnameNs and iriref


    private val triples by (subject and predicateObjectList) or (blankNodePropertyList and optional(predicateObjectList)) map { (subj, predObjList) ->
        val triplesResult = buildList {
            if (predObjList != null) {
                val (pred, objList, semicolonRestList) = predObjList
                objList.forEach {
                    add(RdfTriple(subj, pred, it))
                }
                semicolonRestList.forEach {
                    val (semiPred, semiObjList) = it ?: return@forEach
                    semiObjList.forEach { semiObj ->
                        add(
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
                addAll(blankNodeTriplesSet)
                blankNodeTriplesSet.clear()
            }
            if (collectionTripleSet.isNotEmpty()) {
                addAll(collectionTripleSet)
                collectionTripleSet.clear()
            }
        }
        val freeVariables = varSet.toSet()
        varSet.clear()
        val collectionBlankNodes = blankNodeToDirectParentSet.toSet()
        blankNodeToDirectParentSet.clear()
        Triple(triplesResult, freeVariables, collectionBlankNodes)
    }

    private val emptyVarList by (lpar and rpar) use { listOf<BlankNode>() }
    private val variableList by emptyVarList or (-lpar and oneOrMore(blankNode) and -rpar)

    // Triple(hayesGraph, freeVariables, collectionBlankNodes)
    private val rdfSurfacesParser: Parser<Triple<List<HayesGraphElement>, Set<BlankNode>, Set<BlankNode>>> by
    (variableList and
            (iri) and
            -lparcurl and
            (oneOrMore(parser(this::rdfSurfacesParser)) or optional(space).use {
                listOf()
            }) and
            -rparcurl) map { (variableList, surface, rest) ->
        val (hayeGraph, freeVariables, freeCollectionBlankNodes) = rest.reduceOrNull { acc, triple ->
            Triple(acc.first.plus(triple.first), acc.second.plus(triple.second), acc.third.plus(triple.third))
        } ?: Triple(listOf(), setOf(), setOf())
        val graffiti = variableList.plus(freeCollectionBlankNodes)
        val newHayeGraph = buildList<HayesGraphElement> {
            when (surface.iri) {
                IRIConstants.LOG_POSITIVE_SURFACE_IRI -> this.add(PositiveSurface(graffiti, hayeGraph))
                IRIConstants.LOG_NEGATIVE_TRIPLE_IRI -> this.add(NegativeTripleSurface(graffiti, hayeGraph))
                IRIConstants.LOG_QUERY_SURFACE_IRI -> this.add(QuerySurface(graffiti, hayeGraph))
                IRIConstants.LOG_NEGATIVE_SURFACE_IRI -> this.add(NegativeSurface(graffiti, hayeGraph))
                IRIConstants.LOG_NEUTRAL_SURFACE_IRI -> this.add(NeutralSurface(graffiti, hayeGraph))
                else -> throw RDFSurfacesParseException(message = "Surface IRI not supported")
            }
        }
        Triple(newHayeGraph, freeVariables.minus(variableList.toSet()), setOf<BlankNode>())
    } or triples and -optional(dot)

    private val directive by (prefixID or sparqlPrefix).map { (prefix, iriStr) ->
        val iri = IRI.from(iriStr).let {
            it.takeUnless { it.isRelativeReference() } ?: IRI.transformReference(it, baseIri)
        }
        prefixMap.put(prefix.text.dropLast(1), iri.iri)
    } or (base or sparqlBase).map { iriStr ->
        baseIri = IRI.from(iriStr)
            .let { it.takeUnless { it.isRelativeReference() } ?: IRI.transformReference(it, baseIri) }
    } use { null }

    private val statement by directive or rdfSurfacesParser
    private val turtleDoc by oneOrMore(statement) map {
        val (hayesGraph, freeVariables, freeCollectionBlankNodes) = it.reduceOrNull { acc, triple ->
            when {
                acc == null -> triple
                triple == null -> acc
                else -> Triple(
                    acc.first.plus(triple.first),
                    acc.second.plus(triple.second),
                    acc.third.plus(triple.third)
                )
            }
        } ?: Triple(listOf(), setOf(), setOf())
        hayesGraph.singleOrNull() as? PositiveSurface ?: PositiveSurface(
            freeCollectionBlankNodes.plus(freeVariables).toList(),
            hayesGraph
        )
    }

    override val rootParser: Parser<PositiveSurface>
        get() {
            return try {
                turtleDoc.getValue(this, this::rootParser)
            } finally {
                resetAll()
            }
        }


    private fun createBlankNodeId(): String = "$bnLabel${++blankNodeCounter}"

    fun resetAll() {
        blankNodeCounter = 0
        varSet.clear()
        blankNodeTriplesSet.clear()
        prefixMap.clear()
        collectionTripleSet.clear()
        bnLabel = "BN_"
        blankNodeToDirectParentSet.clear()
    }

    //https://www.w3.org/TR/turtle/#numeric
    private fun replaceNumericEscapeSequences(string: String) = string.replace("\\\\u[0-9A-Fa-f]{4}".toRegex()) {
        Integer.parseInt(it.value.removePrefix("\\u"), 16).toChar().toString()
    }.replace("\\\\U[0-9A-Fa-f]{8}".toRegex()) {
        Integer.parseInt(it.value.removePrefix("\\U"), 16).toChar().toString()
    }

    private fun replaceReservedCharacterEscapes(string: String) =
        string.replace("\\\\[~.!$&'()*+,;=/?#@%_-]".toRegex()) {
            it.value.removePrefix("\\")
        }

    private fun replaceStringEscapes(string: String) = string
        .replace("\\t", "\u0009")
        .replace("\\b", "\u0008")
        .replace("\\n", "\u000A")
        .replace("\\r", "\u000D")
        .replace("\\f", "\u000C")
        .replace("\\\"", "\u0022")
        .replace("\\'", "\u0027")
        .replace("\\\\'", "\u005C")


    fun tryParseToEnd(input: String, baseIRI: IRI): ParseResult<PositiveSurface> {
        var bnLabel = "BN_"
        var i = 0
        while (input.contains("\\b_:$bnLabel\\d+\\b".toRegex()) && i++ in 0..10) {
            bnLabel += '0'
        }
        if (i == 11) throw RDFSurfacesParseException("Invalid blank node Label. Please rename all blank node labels that have the form 'BN_[0-9]+'.")
        this.bnLabel = bnLabel
        this.baseIri = baseIRI
        return rootParser.tryParseToEnd(tokenizer.tokenize(input), 0)
    }

    fun parseToEnd(input: String, baseIRI: IRI): PositiveSurface {
        var bnLabel = "BN_"
        var i = 0
        while (input.contains("\\b_:$bnLabel\\d+\\b".toRegex()) && i++ in 0..10) {
            bnLabel += '0'
        }
        if (i > 10) throw RDFSurfacesParseException("Invalid blank node Label. Please rename all blank node labels that have the form 'BN_[0-9]+'.")
        this.bnLabel = bnLabel
        this.baseIri = baseIRI
        return rootParser.parseToEnd(tokenizer.tokenize(input))
    }
}