package parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import rdfSurfaces.BlankNode
import rdfSurfaces.Collection
import rdfSurfaces.IRI
import rdfSurfaces.RdfTripleElement

object VampireQuestionAnsweringResultsParser :
    Grammar<Pair<List<List<RdfTripleElement>>, List<List<List<RdfTripleElement>>>>>() {

    val results = mutableListOf<List<RdfTripleElement>>()
    val orResults = mutableListOf<List<List<RdfTripleElement>>>()

    val space by regexToken("\\s+", ignore = true)

    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val lparBracket by literalToken("[")
    val rparBracket by literalToken("]")
    val comma by literalToken(",")
    val verticalBar by literalToken("|")
    val underscore by literalToken("_")
    val listLiteral by literalToken("list")
    val variableToken by regexToken("([^\\s'\\[\\](),|])+")
    val variable by variableToken use { BlankNode(this.text) }
    val iriToken by regexToken("'([^\\s'\\[\\](),|])+'")
    val iri by iriToken use { IRI(this.text.removeSurrounding("'")) }
    val list by -listLiteral and -lpar and (variable or iri) and zeroOrMore(-comma and (variable or iri)) and -rpar use {
        Collection(
            this.t2.plus(this.t1)
        )
    }
    val skToken by regexToken("sK\\d+\\(([^\\s)\\[\\]])+\\)")
    val plainSkToken by regexToken("sK\\d+")
    val sk by skToken or plainSkToken use { IRI(this.text) }

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
    }) and zeroOrMore(-comma and (variableList.map { results.add(it) } or multipleVariableList.map { orResults.add(it) }))) and -verticalBar and -underscore and -rparBracket

    override val rootParser by resultValue use {
        try {
            results.toList() to orResults.toList()
        } finally {
            results.clear()
            orResults.clear()
        }
    }
}