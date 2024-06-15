package parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import model.rdf_term.*
import model.rdf_term.Collection
import util.NotSupportedException

object TptpTupleAnswerFormTransformer :
    Grammar<Pair<List<List<RdfTerm>>, List<List<List<RdfTerm>>>>>() {

    private val results = mutableListOf<List<RdfTerm>>()
    private val orResults = mutableListOf<List<List<RdfTerm>>>()

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
    private val term: Parser<RdfTerm> by variable or parser(this::nonLogicalSymbol)

    private val arguments: Parser<List<RdfTerm>> by (term and oneOrMore(-comma and term)).map {
        buildList {
            add(it.t1)
            it.t2.forEach { rdfTripleElement ->
                add(rdfTripleElement)
            }
        }
    } or term.map { listOf(it) }

    private val nonLogicalSymbol: Parser<RdfTerm> by (atomicWord and -lpar and arguments and -rpar).map { (atomicWord, arguments) ->
        return@map when {
            atomicWord.text.startsWith("sK") -> BlankNode(
                encodeToValidBlankNodeLabel(
                    atomicWord.text + "-" + arguments.hashCode()
                )
            )

            atomicWord.text.startsWith("list") -> {
                if (arguments.size != 2) throw NotSupportedException("A list can only have two arguments")
                val left = arguments[0]
                val right = arguments[1] as? Collection ?: throw NotSupportedException("Malformed list input")
                CollectionPair(left, right)
            }
            else -> throw NotSupportedException("Function/Predicate Symbol of TPTP tuple answer not supported")
        }
    } or atomicWord.map { atomicWord ->
        return@map when {
            atomicWord.text.startsWith("sK") -> BlankNode(atomicWord.text)
            atomicWord.text.startsWith("'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'") -> CollectionEnd
            else -> {
                getLiteralFromStringOrNull(atomicWord.text)
                    ?: getLangLiteralFromStringOrNull(atomicWord.text)
                    ?: IRI.from(atomicWord.text.removeSurrounding("'"))
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

    override val rootParser: Parser<Pair<List<List<RdfTerm>>, List<List<List<RdfTerm>>>>>
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

    private fun getLiteralFromStringOrNull(literal: String) =
        ("\"(.*)\"\\^\\^(.+)".toRegex()).matchEntire(literal.removeSurrounding("'"))?.let {
            val (literalValue, datatypeIri) = it.destructured
            DefaultLiteral.fromNonNumericLiteral(literalValue, IRI.from(datatypeIri))
        }


    private fun getLangLiteralFromStringOrNull(literal: String) =
        ("\"(.*)\"@(.+)".toRegex()).matchEntire(literal.removeSurrounding("'"))?.let {
            val (literalValue, languageTag) = it.destructured
            LanguageTaggedString(literalValue, languageTag)
        }
}