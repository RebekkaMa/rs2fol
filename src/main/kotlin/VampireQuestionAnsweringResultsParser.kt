import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

object VampireQuestionAnsweringResultsParser : Grammar<Pair<List<String>,List<String>>>() {

    val results = mutableListOf<String>()
    val orResults = mutableListOf<String>()

    val space by regexToken("\\s+", ignore = true)

    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val lparBracket by literalToken("[")
    val rparBracket by literalToken("]")
    val comma by literalToken(",")
    val verticalBar by literalToken("|")
    val underscore by literalToken("_")
    val iri by regexToken("'([^\\s'\\[\\](),|])+'")
    val bn by regexToken("([^\\s'\\[\\](),|])+")

    val variableList = lparBracket and ((iri or bn) and zeroOrMore(comma and (iri or bn))) and rparBracket use {this.t1.text + this.t2.t1.text + this.t2.t2.joinToString(separator = ""){it.t1.text + (it.t2.text) } + this.t3.text}
    val multipleVariableList = lpar and (variableList and oneOrMore(verticalBar and variableList)) and rpar use {this.t1.text + this.t2.t1 + this.t2.t2.joinToString(separator = ""){it.t1.text + it.t2} + this.t3.text}
    val resultValue by -lparBracket and optional((variableList.map{results.add(it)} or multipleVariableList.map{orResults.add(it)}) and zeroOrMore(-comma and (variableList.map{results.add(it)} or multipleVariableList.map{orResults.add(it)}))) and -verticalBar and -underscore and -rparBracket

    override val rootParser: Parser<Pair<List<String>, List<String>>> by resultValue use {
        try {
            results.toList() to orResults.toList()
        } finally {
            results.clear()
            orResults.clear()
        }
    }
}