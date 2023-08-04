import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import parser.TPTPTupleAnswerParser
import rdfSurfaces.BlankNode

class TPTPTupleAnswerParser : ShouldSpec(
    {
        should("do the right thing"){
            val str = "[(['http://example.org/ns#beetle','http://example.org/ns#nice']|['http://example.org/ns#beetle','http://example.org/ns#green']),(['http://example.org/ns#beetle','http://example.org/ns#blue']|['http://example.org/ns#beetle','http://example.org/ns#green']),['http://example.org/ns#beetle','http://example.org/ns#beautiful']|_]"
            println( TPTPTupleAnswerParser.parseToEnd(str))
        }

        should("do the right things with lists"){
            val str = "[[list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s','\"0\"^^http://www.w3.org/2001/XMLSchema#integer'))))),list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s','\"0\"^^http://www.w3.org/2001/XMLSchema#integer'))))))))))))))))))))]|_]"
            println( TPTPTupleAnswerParser.parseToEnd(str))
        }

        should("parse skolem function"){
            val str = "[[sK1('http://example.org/ns#b','http://example.org/ns#c')]|_]"
            TPTPTupleAnswerParser.parseToEnd(str) shouldBeEqualToComparingFields Pair(listOf(listOf( BlankNode("sK1"))),listOf())
        }

    }
)
