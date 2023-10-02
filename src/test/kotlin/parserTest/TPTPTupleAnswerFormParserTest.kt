package parserTest

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import parser.TptpTupleAnswerFormTransformer
import rdfSurfaces.BlankNode
import rdfSurfaces.Collection
import rdfSurfaces.IRI
import rdfSurfaces.Literal
import util.NotSupportedException

class TPTPTupleAnswerFormParserTest : ShouldSpec(
    {
        should("parse basic example without exception") {
            val str =
                "[(['http://example.org/ns#beetle','http://example.org/ns#nice']|['http://example.org/ns#beetle','http://example.org/ns#green']),['http://example.org/ns#beetle','http://example.org/ns#beautiful']|_]"
            parser.TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqualToComparingFields Pair(
                listOf(listOf(IRI.from("http://example.org/ns#beetle"), IRI.from("http://example.org/ns#beautiful"))),
                listOf(
                    listOf(
                        listOf(IRI.from("http://example.org/ns#beetle"), IRI.from("http://example.org/ns#nice")),
                        listOf(IRI.from("http://example.org/ns#beetle"), IRI.from("http://example.org/ns#green"))
                    )
                )
            )
        }

        should("parse example with lists without exception") {
            val str =
                "[[list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s','\"0\"^^http://www.w3.org/2001/XMLSchema#integer'))),list]|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqualToComparingFields Pair(
                listOf(
                    listOf(
                        Collection(
                            listOf(
                                IRI.from("http://example.org/ns#s"),
                                Collection(
                                    listOf(
                                        IRI.from("http://example.org/ns#s"),
                                        Collection(
                                            listOf(
                                                IRI.from("http://example.org/ns#s"),
                                                Literal.fromNonNumericLiteral(
                                                    "0",
                                                    IRI.from("http://www.w3.org/2001/XMLSchema#integer")
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        Collection()
                    )
                ),
                listOf()
            )
        }

        should("parse example with rdf literals without exception") {
            val str =
                "[['\"0\"^^http://www.w3.org/2001/XMLSchema#string','\"0\"@en'],['http://example.org/ns#s', sK5]|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqualToComparingFields Pair(
                listOf(
                    listOf(
                        Literal.fromNonNumericLiteral(
                            "0",
                            IRI.from("http://www.w3.org/2001/XMLSchema#string")
                        ),
                        Literal.fromNonNumericLiteral(
                            "0",
                            "en"
                        )
                        ),
                    listOf(
                        IRI.from("http://example.org/ns#s"),
                        BlankNode("sK5")
                    )
                ),
                listOf()
            )
        }


        should("parse example with skolem function without exception") {
            val str = "[[sK1('http://example.org/ns#b','http://example.org/ns#c')]|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqualToComparingFields Pair(
                listOf(
                    listOf(
                        BlankNode(TptpTupleAnswerFormTransformer.encodeToValidBlankNodeLabel("sK1-${listOf(IRI.from("http://example.org/ns#b"), IRI.from("http://example.org/ns#c")).hashCode()}"))
                    )
                ),
                listOf()
            )
        }

        should("parse empty result") {
            val str = "[|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqualToComparingFields Pair(listOf(), listOf())
        }

        should("parse another basic example without exception") {
            val str =
                "[['http://example.org/ns#beetle','\"RDF/XML Syntax Specification (Revised)\"^^http://www.w3.org/2001/XMLSchema#string'],[list('http://example.org/ns#s'),'\"That Seventies Show\"@en'],['http://example.org/ns#beetle','\"http://www.w3.org/2001/XMLSchema#string\"^^http://www.w3.org/2001/XMLSchema#string']|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqualToComparingFields Pair(
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
                    ),
                    listOf(
                        IRI.from("http://example.org/ns#beetle"),
                        Literal.Companion.fromNonNumericLiteral(
                            "http://www.w3.org/2001/XMLSchema#string",
                            IRI.from("http://www.w3.org/2001/XMLSchema#string")
                        )
                    ),

                    ), listOf()
            )
        }

        should("throw an exception") {
            val str =
                "[['http://example.org/ns#beetle','RDF/XML Syntax Specification (Revised)'],[list('http://example.org/ns#s'),'\"That Seventies Show\"@en']|_]"

            shouldThrow<NotSupportedException> {
                TptpTupleAnswerFormTransformer.parseToEnd(str)
            }
        }
    }
)
