import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import parser.TPTPTupleAnswerFormTransformer
import rdfSurfaces.BlankNode
import rdfSurfaces.Collection
import rdfSurfaces.IRI
import rdfSurfaces.Literal

class TPTPTupleAnswerFormParserTest : ShouldSpec(
    {
        should("do the right thing") {
            val str =
                "[(['http://example.org/ns#beetle','http://example.org/ns#nice']|['http://example.org/ns#beetle','http://example.org/ns#green']),(['http://example.org/ns#beetle','http://example.org/ns#blue']|['http://example.org/ns#beetle','http://example.org/ns#green']),['http://example.org/ns#beetle','http://example.org/ns#beautiful']|_]"
            println(TPTPTupleAnswerFormTransformer.parseToEnd(str))
        }

        should("do the right things with lists") {
            val str =
                "[[list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s','\"0\"^^http://www.w3.org/2001/XMLSchema#integer'))))),list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s','\"0\"^^http://www.w3.org/2001/XMLSchema#integer'))))))))))))))))))))]|_]"
            println(TPTPTupleAnswerFormTransformer.parseToEnd(str))
        }

        should("parse skolem function") {
            val str = "[[sK1('http://example.org/ns#b','http://example.org/ns#c')]|_]"
            TPTPTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqualToComparingFields Pair(
                listOf(listOf(BlankNode("sK1(http://example.org/ns#c, http://example.org/ns#b)"))),
                listOf()
            )
        }

        should("parse empty") {
            val str = "[|_]"
            TPTPTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqualToComparingFields Pair(listOf(), listOf())
        }

        should("parse elements") {
            val str =
                "[['http://example.org/ns#beetle','\"RDF/XML Syntax Specification (Revised)\"^^http://www.w3.org/2001/XMLSchema#string'],[list('http://example.org/ns#s'),'\"That Seventies Show\"@en']|_]"
            TPTPTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqualToComparingFields Pair(
                listOf(
                    listOf(
                        IRI.from("http://example.org/ns#beetle"),
                        Literal.Companion.fromNonNumericLiteral(
                            "RDF/XML Syntax Specification (Revised)",
                            IRI.from("http://www.w3.org/2001/XMLSchema#string")
                        )
                    ),
                    listOf(
                        Collection(listOf(IRI.from("http://example.org/ns#s"))),
                        Literal.fromNonNumericLiteral("That Seventies Show", "en")
                    )
                ), listOf()
            )
        }

    }
)
