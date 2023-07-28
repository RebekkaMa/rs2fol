package parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import org.apache.jena.atlas.iterator.Iter.takeWhile
import rdfSurfaces.BlankNode
import rdfSurfaces.Collection
import rdfSurfaces.IRI
import rdfSurfaces.RdfTripleElement

object TPTPTupleAnswerParser :
    Grammar<Pair<List<List<RdfTripleElement>>, List<List<List<RdfTripleElement>>>>>() {

    private val results = mutableListOf<List<RdfTripleElement>>()
    private val orResults = mutableListOf<List<List<RdfTripleElement>>>()

    val space by regexToken("\\s+", ignore = true)

    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val lparBracket by literalToken("[")
    private val rparBracket by literalToken("]")
    private val comma by literalToken(",")
    private val verticalBar by literalToken("|")
    private val underscore by literalToken("_")
    private val listLiteral by literalToken("list")
    private val variableToken by regexToken("[A-Z]([^\\s'\\[\\](),|])*")
    private val variable by variableToken use { BlankNode(this.text) }
    private val iriToken by regexToken("'([^\\s'\\[\\](),|])+'")
    private val iri by iriToken use { IRI(this.text.removeSurrounding("'")) }
    val list by -listLiteral and -lpar and (variable or iri) and zeroOrMore(-comma and (variable or iri)) and -rpar use {
        Collection(
            this.t2.plus(this.t1)
        )
    }
    val skToken by regexToken("sK\\d+\\(([^\\s)\\[\\]])+\\)")
    val plainSkToken by regexToken("sK\\d+")
    //TODO()
    val sk by skToken or plainSkToken use { BlankNode(this.text.takeWhile { it != '(' }) }

    private val variableList by
    -lparBracket and ((sk or list or iri or variable) and zeroOrMore(-comma and (sk or list or iri or variable))) and -rparBracket use {
        //TODO(Performance)
        listOf(this.t1).plus(this.t2)
    }

    private val multipleVariableList by (-lpar and (variableList and oneOrMore(-verticalBar and variableList)) and -rpar) use {
        listOf(this.t1).plus(this.t2)
    }

    private val resultValue by -lparBracket and optional((variableList.map { results.add(it) } or multipleVariableList.map {
        orResults.add(
            it
        )
    }) and zeroOrMore(-comma and (variableList.map { results.add(it) } or multipleVariableList.map { orResults.add(it) }))) and -optional(verticalBar) and -optional(underscore) and -rparBracket

    override val rootParser by resultValue use {
        try {
            results.toList() to orResults.toList()
        } finally {
            results.clear()
            orResults.clear()
        }
    }
}