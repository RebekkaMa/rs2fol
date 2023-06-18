import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.core.spec.style.ShouldSpec

class VampireQuestionAnsweringResultsParserTest : ShouldSpec(
    {
        should("do the right thing"){
            val str = "[(['http://example.org/ns#beetle','http://example.org/ns#nice']|['http://example.org/ns#beetle','http://example.org/ns#green']),(['http://example.org/ns#beetle','http://example.org/ns#blue']|['http://example.org/ns#beetle','http://example.org/ns#green']),['http://example.org/ns#beetle','http://example.org/ns#beautiful']|_]"
            println( VampireQuestionAnsweringResultsParser.parseToEnd(str))
        }

        should("do the right things with lists"){
            val str = "[[list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s','\"0\"^^http://www.w3.org/2001/XMLSchema#integer'))))),list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s',list2('http://example.org/ns#s','\"0\"^^http://www.w3.org/2001/XMLSchema#integer'))))))))))))))))))))]|_]"
            println( VampireQuestionAnsweringResultsParser.parseToEnd(str))

        }

    }
)
