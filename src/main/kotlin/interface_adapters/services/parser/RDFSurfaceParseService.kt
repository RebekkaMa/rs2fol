package interface_adapters.services.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import entities.rdfsurfaces.*
import entities.rdfsurfaces.rdf_term.*
import entities.rdfsurfaces.rdf_term.Collection
import interface_adapters.services.parser.util.*
import use_cases.modelTransformer.SurfaceNotSupportedError
import util.*
import util.IRIConstants.RDF_TYPE_IRI
import util.commandResult.Error
import util.commandResult.IntermediateStatus

typealias HayesGraph = List<HayesGraphElement>
typealias FreeVariables = Set<BlankNode>
typealias CollectionBlankNodes = Set<BlankNode>
typealias InterimParseResult = Triple<HayesGraph, FreeVariables, CollectionBlankNodes>


class RDFSurfaceParseService(val useRDFLists: Boolean) : Grammar<PositiveSurface>() {

    private object BlankNodeCounter {
        private var count: Int = 0

        fun reset() {
            count = 0
        }

        fun getAndIncrement() = ++count
    }

    private lateinit var bnLabel: String

    private val lineComment by regexToken("^\\s*#.*$".toRegex(RegexOption.MULTILINE), ignore = true)
    private val inlineComment by regexToken("\\s#.*$".toRegex(RegexOption.MULTILINE), ignore = true)

    private val space by regexToken("\\s+", ignore = true)

    private val blankNodeTriplesSet = mutableListOf<RdfTriple>()
    private val prefixMap = mutableMapOf<String, String>()
    private var baseIri: IRI = IRI.from("file://" + System.getProperty("user.dir") + "/")

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
    private val lqcurl by literalToken("<<")
    private val rqcurl by literalToken(">>")

    private val prefixIdStart by literalToken("@prefix")
    private val baseStart by literalToken("@base")
    private val sparqlBaseStart by regexToken("\\bBASE\\b".toRegex(RegexOption.IGNORE_CASE))
    private val sparqlPrefixStart by regexToken("\\bPREFIX\\b".toRegex(RegexOption.IGNORE_CASE))

    private val iriRefToken by regexToken("<([^\u0000-\u0020<>\"{}|^`\\\\]|((\\\\u([0-9]|[A-F]|[a-f]){4})|(\\\\U([0-9]|[A-F]|[a-f]){8})))*>")
    private val iriRef by iriRefToken use { replaceNumericEscapeSequences(text.removeSurrounding("<", ">")) }

    private val blankNodeLabelToken by regexToken("_:($pnCharsU|[0-9])(($pnChars|\\.)*$pnChars)?")

    private val pNameLn by regexToken("$pnPrefix?:$pnLocal")
    private val pNameNs by regexToken("$pnPrefix?:")
    private val prefixedName by pNameLn.use {
        val (prefix, local) = text.split(':', limit = 2)
        return@use (prefixMap[prefix]
            ?: throw UndefinedPrefixException(prefix = prefix)) + replaceReservedCharacterEscapes(local)
    } or pNameNs.use {
        prefixMap[text.trimEnd().dropLast(1)] ?: throw UndefinedPrefixException(text.trimEnd().dropLast(1))
    } map { IRI.from(it) }

    private val iri by iriRef.map { iriString ->
        IRI.from(iriString).let {
            it.takeUnless { it.isRelativeReference() } ?: IRI.transformReference(R = it, B = baseIri)
        }
    } or prefixedName


    private val blankNodeLabel by blankNodeLabelToken.use {
        BlankNode(
            this.text.drop(
                2
            )
        )
    }
    private val anon by lparBracket and rparBracket
    private val blankNode by blankNodeLabel or anon.use {
        BlankNode(
            createBlankNodeId()
        )
    }

    private val numericLiteralToken by regexToken("(?<!:)\\b((([+-]?([0-9]+\\.[0-9]*([eE][+-]?[0-9]+)))|(\\.[0-9]+([eE][+-]?[0-9]+))|([0-9]+([eE][+-]?[0-9]+)))|([+-]?[0-9]*\\.[0-9]+)|([+-]?[0-9]+))\\b")
    private val numericLiteral by numericLiteralToken use { DefaultLiteral.fromNumericLiteral(this.text) }

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
            null -> DefaultLiteral(
                lexicalValue = replaceStringEscapes(replaceNumericEscapeSequences(this.t1)),
                datatypeIRI = IRI.from(IRIConstants.XSD_STRING_IRI)
            )

            is String -> LanguageTaggedString(lexicalValue = this.t1, langTag = part.drop(1))
            is IRI -> {
                val literalValue =
                    this.t1.takeUnless { part.iri == IRIConstants.XSD_STRING_IRI } ?: replaceStringEscapes(
                        replaceNumericEscapeSequences(this.t1)
                    )
                DefaultLiteral(literalValue, datatypeIRI = part)
            }

            else -> throw LiteralNotValidException(value = t1, iri = (t2.toString()))
        }
    }
    private val booleanLiteralToken by regexToken("(true)|(false)")
    private val booleanLiteral by booleanLiteralToken use {
        DefaultLiteral(
            lexicalValue = text,
            datatypeIRI = IRI.from(
                IRIConstants.XSD_BOOLEAN_IRI
            )
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

    private val collection: Parser<RdfTerm> by -lpar and zeroOrMore(parser(this::rdfObject)) and -rpar use {
        if (useRDFLists) {
            if (this.isEmpty()) IRI.from(IRIConstants.RDF_NIL_IRI) else {
                val collectionStart = BlankNode(createBlankNodeId())
                blankNodeToDirectParentSet.add(collectionStart)
                this.foldIndexed(collectionStart) { index, acc, s ->
                    collectionTripleSet.add(
                        RdfTriple(
                            acc,
                            IRI.from(IRIConstants.RDF_FIRST_IRI),
                            s
                        )
                    )
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
    private val predicate by iri.map {
        when (it.iri) {
            IRIConstants.LOG_POSITIVE_SURFACE_IRI,
            IRIConstants.LOG_NEGATIVE_TRIPLE_IRI,
            IRIConstants.LOG_QUERY_SURFACE_IRI,
            IRIConstants.LOG_NEGATIVE_SURFACE_IRI,
            IRIConstants.LOG_NEUTRAL_SURFACE_IRI,
            IRIConstants.LOG_QUESTION_SURFACE_IRI,
            IRIConstants.LOG_ANSWER_SURFACE_IRI,
            IRIConstants.LOG_NEGATIVE_COMPONENT_SURFACE_IRI,
            IRIConstants.LOG_NEGATIVE_ANSWER_SURFACE_IRI -> throw ParseException(InvalidSyntax())

            else -> it
        }
    } or blankNode.map { varSet.add(it); it } or literal
    private val rdfObject: Parser<RdfTerm> by iri or blankNode.map { varSet.add(it); it } or literal or blankNodePropertyList or collection

    private val verb by predicate or a.map { IRI.from(RDF_TYPE_IRI) } or literal or iri

    private val objectList by rdfObject and zeroOrMore(-comma and rdfObject) use { listOf(this.t1).plus(this.t2) }
    private val predicateObjectList by verb and objectList and zeroOrMore(
        -semicolon and optional(verb and objectList)
    )

    private val prefixID by -prefixIdStart and pNameNs and iriRef and -dot
    private val base by -baseStart and iriRef and -dot
    private val sparqlBase by -sparqlBaseStart and iriRef
    private val sparqlPrefix by -sparqlPrefixStart and pNameNs and iriRef


    private val triples: Parser<InterimParseResult> by (subject and predicateObjectList) or (blankNodePropertyList and optional(
        predicateObjectList
    )) map { (subj, predObjList) ->
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
        InterimParseResult(triplesResult, freeVariables, collectionBlankNodes)
    }

    private val emptyVarList by (lpar and rpar) use { listOf<BlankNode>() }
    private val variableList by emptyVarList or (-lpar and oneOrMore(blankNode) and -rpar)

    private val rdfSurfacesParser: Parser<InterimParseResult> by
    (variableList and
            (iri) and
            (
                    (-lparcurl and (oneOrMore(parser(this::rdfSurfacesParser)) or optional(space).use { listOf() }) and -rparcurl)
                            or (-lqcurl and triples and -rqcurl).use { listOf(this) }
                    )
            ) map { hayesTriple ->
        val (variableList, surface, rest) = hayesTriple
        val (hayesGraph, freeVariables, freeCollectionBlankNodes) = rest.reduceOrNull { acc, (hayesGraph, freeVariables, freeCollectionBlankNodes) ->
            Triple(acc.first.plus(hayesGraph), acc.second.plus(freeVariables), acc.third.plus(freeCollectionBlankNodes))
        } ?: Triple(listOf(), setOf(), setOf())
        val graffiti = variableList.plus(freeCollectionBlankNodes)
        val newHayeGraph = buildList<HayesGraphElement> {
            when (surface.iri) {
                IRIConstants.LOG_POSITIVE_SURFACE_IRI -> this.add(PositiveSurface(graffiti, hayesGraph))
                IRIConstants.LOG_NEGATIVE_TRIPLE_IRI -> this.add(NegativeTripleSurface(graffiti, hayesGraph))
                IRIConstants.LOG_QUERY_SURFACE_IRI -> this.add(QuerySurface(graffiti, hayesGraph))
                IRIConstants.LOG_NEGATIVE_SURFACE_IRI -> this.add(NegativeSurface(graffiti, hayesGraph))
                IRIConstants.LOG_NEUTRAL_SURFACE_IRI -> this.add(NeutralSurface(graffiti, hayesGraph))
                IRIConstants.LOG_QUESTION_SURFACE_IRI -> this.add(QuestionSurface(graffiti, hayesGraph))
                IRIConstants.LOG_ANSWER_SURFACE_IRI -> this.add(AnswerSurface(graffiti, hayesGraph))
                IRIConstants.LOG_NEGATIVE_ANSWER_SURFACE_IRI -> this.add(NegativeAnswerSurface(graffiti, hayesGraph))
                IRIConstants.LOG_NEGATIVE_COMPONENT_SURFACE_IRI -> this.add(
                    NegativeComponentSurface(
                        graffiti,
                        hayesGraph
                    )
                )

                else -> throw SurfaceNotSupportedException(surface = surface.iri)
            }
        }
        InterimParseResult(
            first = newHayeGraph,
            second = freeVariables.minus(variableList.toSet()),
            third = setOf()
        )
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
                else -> InterimParseResult(
                    acc.first.plus(triple.first),
                    acc.second.plus(triple.second),
                    acc.third.plus(triple.third)
                )
            }
        } ?: InterimParseResult(listOf(), setOf(), setOf())
        hayesGraph.singleOrNull() as? PositiveSurface ?: PositiveSurface(
            graffiti = freeCollectionBlankNodes.plus(freeVariables).toList(),
            hayesGraph = hayesGraph
        )
    }

    override val rootParser: Parser<PositiveSurface>
        get() {
            resetAll()
            return turtleDoc.getValue(this, this::rootParser)
        }


    private fun createBlankNodeId(): String = "$bnLabel${BlankNodeCounter.getAndIncrement()}"

    private fun resetAll() {
        BlankNodeCounter.reset()
        varSet.clear()
        blankNodeTriplesSet.clear()
        prefixMap.clear()
        collectionTripleSet.clear()
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

    fun parseToEnd(input: String, baseIRI: IRI): IntermediateStatus<PositiveSurface, Error> {
        var bnLabel = "BN_"
        var i = 0
        while (input.contains("_:$bnLabel\\d+".toRegex()) && i++ in 0..10) {
            bnLabel += '0'
        }
        if (i > 10) return IntermediateStatus.Error(RdfSurfaceParserError.BlankNodeLabelCollision) //("Invalid blank node Label. Please rename all blank node labels that have the form 'BN_[0-9]+'.")
        this.bnLabel = bnLabel
        this.baseIri = baseIRI
        return try {
            IntermediateStatus.Result(rootParser.parseToEnd(tokenizer.tokenize(input)))
        } catch (exc: Throwable) {
            when (exc) {
                is SurfaceNotSupportedException -> SurfaceNotSupportedError(surface = exc.surface)
                is UndefinedPrefixException -> RdfSurfaceParserError.UndefinedPrefix(prefix = exc.prefix)
                is LiteralNotValidException -> RdfSurfaceParserError.LiteralNotValid(value = exc.value, iri = exc.iri)
                is ParseException -> RdfSurfaceParserError.GenericInvalidInput(throwable = exc)
                else -> throw exc
            }.let { IntermediateStatus.Error(it) }
        }
    }
}


sealed interface RdfSurfaceParserError : Error {
    data object BlankNodeLabelCollision : RdfSurfaceParserError
    data class UndefinedPrefix(val prefix: String) : RdfSurfaceParserError
    data class LiteralNotValid(val value: String, val iri: String) : RdfSurfaceParserError
    data class GenericInvalidInput(val throwable: Throwable) : RdfSurfaceParserError
}