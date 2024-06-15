package parserTest

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldNotBeEqualUsingFields
import io.kotest.matchers.equals.shouldBeEqual
import parser.TptpTupleAnswerFormTransformer
import model.rdf_term.*
import model.rdf_term.Collection
import util.NotSupportedException

class TPTPTupleAnswerFormParserTest : ShouldSpec(
    {
        should("parse basic example without exception") {
            val str =
                "[(['http://example.org/ns#beetle','http://example.org/ns#nice']|['http://example.org/ns#beetle','http://example.org/ns#green']),['http://example.org/ns#beetle','http://example.org/ns#beautiful']|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqual Pair(
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
                "[[list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s',list('\"0\"^^http://www.w3.org/2001/XMLSchema#integer','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil')))),'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil']|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqual Pair(
                listOf(
                    listOf(
                        Collection.fromTerms(
                            IRI.from("http://example.org/ns#s"),
                            IRI.from("http://example.org/ns#s"),
                            IRI.from("http://example.org/ns#s"),
                            DefaultLiteral.fromNonNumericLiteral(
                                "0",
                                IRI.from("http://www.w3.org/2001/XMLSchema#integer")
                            )
                        ),
                        CollectionEnd
                    ),
                ),
                listOf()
            )
        }

        should("parse example with rdf literals without exception") {
            val str =
                "[['\"0\"^^http://www.w3.org/2001/XMLSchema#string','\"0\"@en'],['http://example.org/ns#s', sK5]|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqual Pair(
                listOf(
                    listOf(
                        DefaultLiteral.fromNonNumericLiteral(
                            "0",
                            IRI.from("http://www.w3.org/2001/XMLSchema#string")
                        ),
                        LanguageTaggedString(
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
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqual Pair(
                listOf(
                    listOf(
                        BlankNode(
                            TptpTupleAnswerFormTransformer.encodeToValidBlankNodeLabel(
                                "sK1-${
                                    listOf(
                                        IRI.from("http://example.org/ns#b"),
                                        IRI.from("http://example.org/ns#c")
                                    ).hashCode()
                                }"
                            )
                        )
                    )
                ),
                listOf()
            )
        }

        should("parse empty result") {
            val str = "[|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqual Pair(listOf(), listOf())
        }

        should("parse another basic example without exception") {
            val str =
                "[['http://example.org/ns#beetle','\"RDF/XML Syntax Specification (Revised)\"^^http://www.w3.org/2001/XMLSchema#string'],[list('http://example.org/ns#s','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'),'\"That Seventies Show\"@en'],['http://example.org/ns#beetle','\"http://www.w3.org/2001/XMLSchema#string\"^^http://www.w3.org/2001/XMLSchema#string']|_]"
            TptpTupleAnswerFormTransformer.parseToEnd(str) shouldBeEqual Pair(
                listOf(
                    listOf(
                        IRI.from("http://example.org/ns#beetle"),
                        DefaultLiteral.Companion.fromNonNumericLiteral(
                            "RDF/XML Syntax Specification (Revised)",
                            IRI.from("http://www.w3.org/2001/XMLSchema#string")
                        )
                    ),
                    listOf(
                        Collection.fromTerms(
                            IRI.from(
                                "http://example.org/ns#s"
                            )
                        ),
                        LanguageTaggedString("That Seventies Show", "en")
                    ),
                    listOf(
                        IRI.from("http://example.org/ns#beetle"),
                        DefaultLiteral.Companion.fromNonNumericLiteral(
                            "http://www.w3.org/2001/XMLSchema#string",
                            IRI.from("http://www.w3.org/2001/XMLSchema#string")
                        )
                    ),

                    ), listOf()
            )
        }

        should("throw an exception") {
            val str =
                "[['http://example.org/ns#beetle','RDF/XML Syntax Specification (Revised)'],[list('http://example.org/ns#s','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'),'\"That Seventies Show\"@en']|_]"

            shouldThrow<NotSupportedException> {
                TptpTupleAnswerFormTransformer.parseToEnd(str)
            }
        }
    }
)
