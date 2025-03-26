package adapter.parser

import app.interfaces.results.TptpTupleAnswerFormParserResult
import app.interfaces.services.TptpTupleAnswerFormParserService
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import entities.fol.FOLConstant
import entities.fol.FOLFunction
import entities.fol.FOLVariable
import entities.fol.GeneralTerm
import entities.fol.tptp.AnswerTuple
import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import util.commandResult.Result
import util.commandResult.RootError
import util.commandResult.error
import util.commandResult.success

class TptpTupleAnswerFormToModelServiceImpl : Grammar<TPTPTupleAnswerFormAnswer>(), TptpTupleAnswerFormParserService {

    private val results = mutableListOf<AnswerTuple>()
    private val orResults = mutableListOf<List<AnswerTuple>>()

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
    private val variable by variableToken use { FOLVariable(this.text) }

    private val term: Parser<GeneralTerm> by variable or parser(this::nonLogicalSymbol)

    private val arguments: Parser<List<GeneralTerm>> by (term and oneOrMore(-comma and term)).map {
        buildList {
            add(it.t1)
            it.t2.forEach { rdfTripleElement ->
                add(rdfTripleElement)
            }
        }
    } or term.map { listOf(it) }

    private val nonLogicalSymbol: Parser<GeneralTerm> by (atomicWord and -lpar and arguments and -rpar).map { (atomicWord, arguments) ->
        FOLFunction(atomicWord.text, arguments)
    } or atomicWord.map { atomicWord ->
        FOLConstant(atomicWord.text.removeSurrounding("'"))
    }

    private val variableList by
    -lparBracket and optional((nonLogicalSymbol or variable) and zeroOrMore(-comma and (nonLogicalSymbol or variable))) and -rparBracket use {
        this?.let { listOf(this.t1).plus(this.t2) } ?: listOf()
    }

    private val multipleVariableList by (-lpar and (variableList and oneOrMore(-verticalBar and variableList)) and -rpar) use {
        listOf(this.t1).plus(this.t2)
    }

    private val resultValue by
    (-lparBracket
            and (variableList.map { results.add(AnswerTuple(it)) } or multipleVariableList.map {
        orResults.add(it.map {
            AnswerTuple(
                it
            )
        })
    })
            and zeroOrMore(-comma and (variableList.map { results.add(AnswerTuple(it)) } or multipleVariableList.map {
        orResults.add(
            it.map { AnswerTuple(it) })
    }))
            and -optional(verticalBar)
            and -optional(underscore)
            and -rparBracket
            ) or (-lparBracket and -verticalBar and underscore and -rparBracket)

    override val rootParser: Parser<TPTPTupleAnswerFormAnswer>
        get() {
            results.clear()
            orResults.clear()
            return resultValue.getValue(this, this::rootParser)
                .map { TPTPTupleAnswerFormAnswer(results.toList(), orResults.toList()) }
        }

    override fun parseToEnd(answerTuple: String): Result<TptpTupleAnswerFormParserResult.Success.Parsed, RootError> {
        return try {
            success(TptpTupleAnswerFormParserResult.Success.Parsed(rootParser.parseToEnd(tokenizer.tokenize(answerTuple))))
        } catch (exc: Throwable) {
            val parseError = when (exc) {
                is ParseException -> TptpTupleAnswerFormParserResult.Error.GenericInvalidInput(
                    tptpTuple = answerTuple,
                    throwable = exc
                )

                else -> throw exc
            }
            error(parseError)
        }
    }
}