package controllerTest

import controller.Transformer
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.shouldBe
import org.apache.jena.datatypes.xsd.XSDDatatype
import model.*
import model.rdf_term.*
import model.rdf_term.Collection
import java.io.File

class TransformerTest
    : ShouldSpec(
    {

        val transformer = Transformer()

        context("toFOLTest") {
            should("transform example2.n3 without exception") {
                val solutionFile = File("src/test/resources/turtle/example2.p")

                val iri1 = IRI.from("http://example.org/#spiderman")
                val iri2 = IRI.from("http://www.perceive.net/schemas/relationship/enemyOf")
                val iri3 = IRI.from("http://example.org/#green-goblin")

                val rdfTriple = RdfTriple(iri1, iri2, iri3)

                transformer.toFOL(
                    PositiveSurface(
                        listOf(),
                        listOf(rdfTriple)
                    )
                ).replace("\n", " ") shouldBeEqualComparingTo solutionFile.readText().replace("\n", " ")


            }

            should("transform example11.n3 without exception") {
                val solutionFile = File("src/test/resources/turtle/example11.p")

                val iri1 = IRI.from("http://example.org/vocab/show/218")
                val iri2 = IRI.from("http://www.w3.org/2000/01/rdf-schema#label")
                val iri3 = IRI.from("http://example.org/vocab/show/localName")
                val iri4 = IRI.from("http://example.org/vocab/show/blurb")

                val literal1 =
                    DefaultLiteral("That Seventies Show", XSDDatatype.XSDstring)
                val literal2 =
                    LanguageTaggedString("That Seventies Show" , "en")
                val literal3 =
                    LanguageTaggedString("Cette Série des Années Soixante-dix" , "fr")
                val literal4 =
                    LanguageTaggedString("Cette Série des Années Septante" , "fr-be")
                val literal5 = DefaultLiteral(
                    "This is a multi-line                        # literal with embedded new lines and quotes\n" +
                            "literal with many quotes (\"\"\"\"\")\n" +
                            "and up to two sequential apostrophes ('').", XSDDatatype.XSDstring
                )

                val rdfTriple1 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple2 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple3 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple4 = RdfTriple(iri1, iri3, literal2)
                val rdfTriple5 = RdfTriple(iri1, iri3, literal3)
                val rdfTriple6 = RdfTriple(iri1, iri3, literal4)
                val rdfTriple7 = RdfTriple(iri1, iri4, literal5)

                transformer.toFOL(
                    PositiveSurface(
                        listOf(),
                        listOf(rdfTriple1, rdfTriple2, rdfTriple3, rdfTriple4, rdfTriple5, rdfTriple6, rdfTriple7)
                    )
                ).replace("\n", " ") shouldBeEqualComparingTo solutionFile.readText().replace("\n", " ")
            }


            should("transform example23.n3 without exception") {
                val solutionFile = File("src/test/resources/turtle/example23.p")

                val iri1 = IRI.from("http://example.org/stuff/1.0/p")

                val literal1 = DefaultLiteral("w", XSDDatatype.XSDstring)
                val literal2 = DefaultLiteral(1, XSDDatatype.XSDinteger)
                val literal3 = DefaultLiteral(
                    XSDDatatype.XSDdecimal.parse("2.0"),
                    XSDDatatype.XSDdecimal
                )
                val literal4 = DefaultLiteral(3E1, XSDDatatype.XSDdouble)


                val collection1 = Collection.fromTerms(literal2,literal3,literal4)

                val rdfTriple1 = RdfTriple(collection1, iri1, literal1)

                transformer.toFOL(
                    PositiveSurface(
                        listOf(), listOf(rdfTriple1)
                    )
                ).replace("\\s+".toRegex(), "") shouldBeEqualComparingTo solutionFile.readText().replace("\\s+".toRegex(), "")
            }

            context("blogic") {
                should("transform blogic abc.n3s") {
                    val solutionFile = File("src/test/resources/blogic/abc.p")

                    val iri1 = IRI.from("http://example.org/ns#i")
                    val iri2 = IRI.from("http://example.org/ns#A")
                    val iri3 = IRI.from("http://example.org/ns#B")
                    val iri4 = IRI.from("http://example.org/ns#C")
                    val iri5 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")

                    val bnS = BlankNode("S")
                    val bnC = BlankNode("C")

                    val rdfTriple1 = RdfTriple(iri1, iri5, iri2)
                    val rdfTriple2 = RdfTriple(iri1, iri5, iri4)
                    val rdfTriple3 = RdfTriple(bnS, iri5, iri2)
                    val rdfTriple4 = RdfTriple(bnS, iri5, iri3)
                    val rdfTriple5 = RdfTriple(bnS, iri5, iri4)
                    val rdfTriple6 = RdfTriple(bnS, iri5, bnC)

                    val negativeSurface1 = NegativeSurface(listOf(), listOf(rdfTriple2))
                    val negativeSurface21 = NegativeSurface(listOf(), listOf(rdfTriple4))
                    val negativeSurface22 = NegativeSurface(listOf(), listOf(rdfTriple5))
                    val negativeSurface2 =
                        NegativeSurface(listOf(bnS), listOf(rdfTriple3, negativeSurface21, negativeSurface22))
                    val querySurface = QuerySurface(listOf(bnS, bnC), listOf(rdfTriple6))

                    transformer.toFOL(
                        PositiveSurface(
                            listOf(),
                            listOf(rdfTriple1, negativeSurface1, negativeSurface2, querySurface)
                        )
                    ).replace("\n", " ") shouldBeEqualComparingTo solutionFile.readText().replace("\n", " ")

                }
                should("transform blogic abcd.n3s") {
                    val solutionFile = File("src/test/resources/blogic/abcd.p")

                    val iri1 = IRI.from("http://example.org/ns#i")
                    val iri2 = IRI.from("http://example.org/ns#A")
                    val iri3 = IRI.from("http://example.org/ns#B")
                    val iri4 = IRI.from("http://example.org/ns#C")
                    val iri5 = IRI.from("http://example.org/ns#D")

                    val iri6 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")

                    val bnS = BlankNode("S")
                    val bnC = BlankNode("C")

                    val rdfTriple1 = RdfTriple(iri1, iri6, iri2)
                    val rdfTriple2 = RdfTriple(iri1, iri6, iri4)
                    val rdfTriple3 = RdfTriple(iri1, iri6, iri5)
                    val rdfTriple4 = RdfTriple(bnS, iri6, iri2)
                    val rdfTriple5 = RdfTriple(bnS, iri6, iri3)
                    val rdfTriple6 = RdfTriple(bnS, iri6, iri4)
                    val rdfTriple7 = RdfTriple(bnS, iri6, iri5)
                    val rdfTriple8 = RdfTriple(bnS, iri6, bnC)

                    val negativeSurface1 = NegativeSurface(listOf(), listOf(rdfTriple2))
                    val negativeSurface211 = NegativeSurface(listOf(), listOf(rdfTriple3))
                    val negativeSurface21 = NegativeSurface(listOf(), listOf(negativeSurface211))
                    val negativeSurface2 = NegativeSurface(listOf(), listOf(negativeSurface21))


                    val negativeSurface31 = NegativeSurface(listOf(), listOf(rdfTriple5))
                    val negativeSurface32 = NegativeSurface(listOf(), listOf(rdfTriple6))
                    val negativeSurface33 = NegativeSurface(listOf(), listOf(rdfTriple7))
                    val negativeSurface3 = NegativeSurface(
                        listOf(bnS),
                        listOf(rdfTriple4, negativeSurface31, negativeSurface32, negativeSurface33)
                    )
                    val querySurface = QuerySurface(listOf(bnS, bnC), listOf(rdfTriple8))


                    transformer.toFOL(
                        PositiveSurface(
                            listOf(),
                            listOf(rdfTriple1, negativeSurface1, negativeSurface2, negativeSurface3, querySurface)
                        )
                    ).replace("\n", " ") shouldBeEqualComparingTo solutionFile.readText().replace("\n", " ")

                }
            }
        }

        context("decode and encode to TPTP Literal compatible CHAR Set") {
            should("encode and decode 1") {
                val testStr = "The first line\n" +
                        "The second line\n" +
                        "  more"
                val encoded = transformer.encodeToValidTPTPLiteral(testStr)
//                println(encoded)
//                println(transformer.decodeValidTPTPLiteral(encoded))
                testStr shouldBe transformer.decodeValidTPTPLiteral(encoded)
            }

            should("encode and decode 2") {
                val testStr = "The first line\\nThe second line\\n  more"
                val encoded = transformer.encodeToValidTPTPLiteral(testStr)
                testStr shouldBe transformer.decodeValidTPTPLiteral(encoded)
            }

            should("encode and decode 3") {
                val testStr = "The first line\\nThe second line\\n  more"
                val encoded = transformer.encodeToValidTPTPLiteral(testStr)
                testStr shouldBe transformer.decodeValidTPTPLiteral(encoded)
            }
            should("encode and decode 4") {
                val testStr =
                    "This is a multi-line                        # literal with embedded new lines and quotes\n" +
                            "\uD800\uDC00 literal with many quotes (\"\"\"\"\")\n" +
                            "and up to two sequential apostrophes ('')."
                val encoded = transformer.encodeToValidTPTPLiteral(testStr)
                testStr shouldBe transformer.decodeValidTPTPLiteral(encoded)
            }
        }
        context("decode and encode to TPTP Variable compatible CHAR Set") {
            should("encode and decode 1") {
                val testStr = "BN_1"
                val encoded = transformer.encodeToValidTPTPVariable(testStr)
                testStr shouldBe transformer.decodeValidTPTPVariable(encoded)
            }

            should("encode and decode 2") {
                val testStr = "bn_1"
                val encoded = transformer.encodeToValidTPTPVariable(testStr)
                testStr shouldBe transformer.decodeValidTPTPVariable(encoded)
            }

            should("encode and decode 3") {
                val testStr = "jiUd_.a\uD800\uDC00"
                val encoded = transformer.encodeToValidTPTPVariable(testStr)
                testStr shouldBe transformer.decodeValidTPTPVariable(encoded)
            }
            should("encode and decode 4") {
                val testStr = "Ox3A23n_3"
                val encoded = transformer.encodeToValidTPTPVariable(testStr)
                testStr shouldBe transformer.decodeValidTPTPVariable(encoded)
            }
            should("encode and decode 5") {
                val testStr = "Ox3A2P3n_3"
                val encoded = transformer.encodeToValidTPTPVariable(testStr)
                testStr shouldBe transformer.decodeValidTPTPVariable(encoded)
            }
        }
    })