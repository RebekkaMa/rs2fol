package parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import rdfSurfaces.*
import rdfSurfaces.Collection
import util.NotSupportedException

object TptpTupleAnswerFormTransformer :
    Grammar<Pair<List<List<RdfTripleElement>>, List<List<List<RdfTripleElement>>>>>() {

    private val results = mutableListOf<List<RdfTripleElement>>()
    private val orResults = mutableListOf<List<List<RdfTripleElement>>>()

    val space by regexToken("\\s+", ignore = true)

    private val singleQuoted by regexToken("'([\u0020-\u0026]|[\u0028-\\u005B]|[\u005D-\u007E]|(\\\\['\\\\]))+'")

    private val lowerWord by regexToken("[a-z]([a-zA-Z0-9_]*)")

    private val atomicWord by lowerWord or singleQuoted

    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val lparBracket by literalToken("[")
    private val rparBracket by literalToken("]")
    private val comma by literalToken(",")
    private val verticalBar by literalToken("|")
    private val underscore by literalToken("_")
    private val variableToken by regexToken("[A-Z][A-Za-z0-9_]*")
    private val variable by variableToken use { BlankNode(this.text) }

    //TODO(defined Term + System Term)
    private val term: Parser<RdfTripleElement> by variable or parser(this::nonLogicalSymbol)

    private val arguments: Parser<List<RdfTripleElement>> by (term and oneOrMore(-comma and term)).map {
        buildList {
            add(it.t1)
            it.t2.forEach { rdfTripleElement ->
                add(rdfTripleElement)
            }
        }
    } or term.map { listOf(it) }

    private val nonLogicalSymbol: Parser<RdfTripleElement> by (atomicWord and -lpar and arguments and -rpar).map { (atomicWord, arguments) ->
        return@map when {
            atomicWord.text.startsWith("sK") -> BlankNode(
                encodeToValidBlankNodeLabel(
                    atomicWord.text + "-" + arguments.hashCode()
                )
            )

            atomicWord.text.startsWith("list") -> Collection(arguments)
            else -> throw NotSupportedException("Function/Predicate Symbol of TPTP tuple answer not supported")
        }
    } or atomicWord.map { atomicWord ->
        return@map when {
            atomicWord.text.startsWith("sK") -> BlankNode(atomicWord.text)
            atomicWord.text.startsWith("list") -> Collection(emptyList())
            else -> {
                ("\"(.*)\"\\^\\^(.+)".toRegex()).matchEntire(atomicWord.text.removeSurrounding("'"))?.let {
                    val (literalValue, datatypeIri) = it.destructured
                    Literal.fromNonNumericLiteral(literalValue, IRI.from(datatypeIri))
                } ?: ("\"(.*)\"@(.+)".toRegex()).matchEntire(atomicWord.text.removeSurrounding("'"))?.let {
                    val (literalValue, languageTag) = it.destructured
                    Literal.fromNonNumericLiteral(literalValue, languageTag)
                } ?: IRI.from(atomicWord.text.removeSurrounding("'"))
                    .takeUnless { it.isRelativeReference() || it.iri.contains("\\s".toRegex()) }
                ?: throw NotSupportedException("Element \"${atomicWord.text}\" could not be parsed.")
            }
        }
    }

    private val variableList by
    -lparBracket and optional((nonLogicalSymbol or variable) and zeroOrMore(-comma and (nonLogicalSymbol or variable))) and -rparBracket use {
        this?.let { listOf(this.t1).plus(this.t2) } ?: listOf()
    }

    private val multipleVariableList by (-lpar and (variableList and oneOrMore(-verticalBar and variableList)) and -rpar) use {
        listOf(this.t1).plus(this.t2)
    }

    private val resultValue by (-lparBracket and (variableList.map { results.add(it) } or multipleVariableList.map {
        orResults.add(
            it
        )
    }) and zeroOrMore(-comma and (variableList.map { results.add(it) } or multipleVariableList.map { orResults.add(it) })) and -optional(
        verticalBar
    ) and -optional(underscore) and -rparBracket) or (-lparBracket and -verticalBar and underscore and -rparBracket)

    override val rootParser: Parser<Pair<List<List<RdfTripleElement>>, List<List<List<RdfTripleElement>>>>>
        get() {
            results.clear()
            orResults.clear()
            return resultValue.getValue(this, this::rootParser).map { results.toList() to orResults.toList() }
        }

    fun encodeToValidBlankNodeLabel(string: String): String {
        val hexValueForO = Integer.toHexString('O'.code).uppercase().padStart(4, '0')
        val hexValueForx = Integer.toHexString('x'.code).uppercase().padStart(4, '0')
        val strWithoutOx = string.replace("Ox([0-9A-Fa-f]{4})".toRegex()) {
            "Ox${hexValueForO}Ox$hexValueForx" + it.destructured.component1()
        }
        return buildString {
            strWithoutOx.toCharArray().forEachIndexed { i, char ->
                if ((i in 1..(strWithoutOx.length - 2) && "($pnChars|\\.)".toRegex().matches(char.toString())) ||
                    (i == 0 && "($pnCharsU|[0-9])".toRegex().matches(char.toString())) ||
                    i > 0 && "$pnChars".toRegex().matches(char.toString())
                ) this.append(char) else {
                    val hexValue = Integer.toHexString(char.code).padStart(4, '0').uppercase()
                    this.append("Ox$hexValue")
                }
            }
        }
    }

}