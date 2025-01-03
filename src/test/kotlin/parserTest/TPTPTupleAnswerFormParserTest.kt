package parserTest

import domain.entities.rdf_term.*
import domain.entities.rdf_term.Collection
import domain.error.getSuccessOrNull
import io.kotest.core.spec.style.ShouldSpec
import interface_adapters.services.parsing.TptpTupleAnswerFormParserService
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class TPTPTupleAnswerFormParserTest : ShouldSpec(
    {
        should("parse basic example without exception") {
            val str =
                "[(['http://example.org/ns#beetle','http://example.org/ns#nice']|['http://example.org/ns#beetle','http://example.org/ns#green']),['http://example.org/ns#beetle','http://example.org/ns#beautiful']|_]"
            TptpTupleAnswerFormParserService.parseToEnd(str).getSuccessOrNull().shouldNotBeNull() shouldBeEqual Pair(
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
            TptpTupleAnswerFormParserService.parseToEnd(str).getSuccessOrNull().shouldNotBeNull() shouldBeEqual Pair(
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
                                                DefaultLiteral.fromNonNumericLiteral(
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
            TptpTupleAnswerFormParserService.parseToEnd(str).getSuccessOrNull().shouldNotBeNull() shouldBeEqual Pair(
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
            TptpTupleAnswerFormParserService.parseToEnd(str).getSuccessOrNull().shouldNotBeNull() shouldBeEqual Pair(
                listOf(
                    listOf(
                        BlankNode(
                            TptpTupleAnswerFormParserService.encodeToValidBlankNodeLabel(
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
            TptpTupleAnswerFormParserService.parseToEnd(str).getSuccessOrNull().shouldNotBeNull() shouldBeEqual Pair(listOf(), listOf())
        }

        should("parse another basic example without exception") {
            val str =
                "[['http://example.org/ns#beetle','\"RDF/XML Syntax Specification (Revised)\"^^http://www.w3.org/2001/XMLSchema#string'],[list('http://example.org/ns#s'),'\"That Seventies Show\"@en'],['http://example.org/ns#beetle','\"http://www.w3.org/2001/XMLSchema#string\"^^http://www.w3.org/2001/XMLSchema#string']|_]"
            TptpTupleAnswerFormParserService.parseToEnd(str).getSuccessOrNull().shouldNotBeNull() shouldBeEqual Pair(
                listOf(
                    listOf(
                        IRI.from("http://example.org/ns#beetle"),
                        DefaultLiteral.Companion.fromNonNumericLiteral(
                            "RDF/XML Syntax Specification (Revised)",
                            IRI.from("http://www.w3.org/2001/XMLSchema#string")
                        )
                    ),
                    listOf(
                        Collection(
                            listOf(
                                IRI.from(
                                    "http://example.org/ns#s"
                                )
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
                "[['http://example.org/ns#beetle','RDF/XML Syntax Specification (Revised)'],[list('http://example.org/ns#s'),'\"That Seventies Show\"@en']|_]"

                TptpTupleAnswerFormParserService.parseToEnd(str).isFailure.shouldBe(true)
        }
    }
)
