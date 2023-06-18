import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.core.spec.style.ShouldSpec

class VampireQuestionAnsweringResultsParserTest : ShouldSpec(
    {
        should("do the right thing"){
            val str = "[(['http://example.org/ns#beetle','http://example.org/ns#nice']|['http://example.org/ns#beetle','http://example.org/ns#green']),(['http://example.org/ns#beetle','http://example.org/ns#blue']|['http://example.org/ns#beetle','http://example.org/ns#green']),['http://example.org/ns#beetle','http://example.org/ns#beautiful']|_]"
            println( VampireQuestionAnsweringResultsParser.parseToEnd(str))
        }

    }
)
