import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import org.apache.jena.datatypes.xsd.XSDDatatype
import parser.RDFSurfacesParser
import rdfSurfaces.*
import rdfSurfaces.Collection
import util.RDFSurfacesParseException
import java.io.File

class RDFSurfacesParserTest : ShouldSpec(
    {

        context("selected Turtle examples") {
            should("transform example2.n3 without exception") {
                val file = File("src/test/resources/turtle/example2.n3")

                val iri1 = IRI.from("http://example.org/#spiderman")
                val iri2 = IRI.from("http://www.perceive.net/schemas/relationship/enemyOf")
                val iri3 = IRI.from("http://example.org/#green-goblin")

                val rdfTriple = RdfTriple(iri1, iri2, iri3)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple)
                )
            }

            should("transform example3.n3 without exception") {
                val file = File("src/test/resources/turtle/example3.n3")

                val iri1 = IRI.from("http://example.org/#spiderman")
                val iri2 = IRI.from("http://www.perceive.net/schemas/relationship/enemyOf")
                val iri3 = IRI.from("http://example.org/#green-goblin")
                val iri4 = IRI.from("http://xmlns.com/foaf/0.1/name")
                val literal = Literal("Spiderman", XSDDatatype.XSDstring)

                val rdfTriple1 = RdfTriple(iri1, iri2, iri3)
                val rdfTriple2 = RdfTriple(iri1, iri4, literal)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, rdfTriple2)
                )
            }

            should("transform example4.n3 without exception") {
                val file = File("src/test/resources/turtle/example4.n3")

                val iri1 = IRI.from("http://example.org/#spiderman")
                val iri2 = IRI.from("http://www.perceive.net/schemas/relationship/enemyOf")
                val iri3 = IRI.from("http://example.org/#green-goblin")
                val iri4 = IRI.from("http://xmlns.com/foaf/0.1/name")
                val literal = Literal("Spiderman", XSDDatatype.XSDstring)

                val rdfTriple1 = RdfTriple(iri1, iri2, iri3)
                val rdfTriple2 = RdfTriple(iri1, iri4, literal)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, rdfTriple2)
                )

            }
            should("transform example5.n3 without exception") {
                val file = File("src/test/resources/turtle/example5.n3")

                val iri1 = IRI.from("http://example.org/#spiderman")
                val iri2 = IRI.from("http://xmlns.com/foaf/0.1/name")
                val literal1 = Literal("Spiderman", XSDDatatype.XSDstring)
                val literal2 = LanguageTaggedString("Человек-паук" to "ru")


                val rdfTriple1 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple2 = RdfTriple(iri1, iri2, literal2)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, rdfTriple2)
                )
            }
            should("transform example6.n3 without exception") {
                val file = File("src/test/resources/turtle/example6.n3")

                val iri1 = IRI.from("http://example.org/#spiderman")
                val iri2 = IRI.from("http://xmlns.com/foaf/0.1/name")
                val literal1 = Literal("Spiderman", XSDDatatype.XSDstring)
                val literal2 = LanguageTaggedString("Человек-паук" to "ru")


                val rdfTriple1 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple2 = RdfTriple(iri1, iri2, literal2)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, rdfTriple2)
                )
            }
            should("transform example7.n3 without exception") {
                val file = File("src/test/resources/turtle/example7.n3")

                val iri1 = IRI.from("http://example.org/#green-goblin")
                val iri2 = IRI.from("http://www.perceive.net/schemas/relationship/enemyOf")
                val iri3 = IRI.from("http://example.org/#spiderman")

                val rdfTriple1 = RdfTriple(iri1, iri2, iri3)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1)
                )
            }

            should("transform example8.n3 without exception") {
                val file = File("src/test/resources/turtle/example8.n3")

                val iri1 = IRI.from("http://example.org/#green-goblin")
                val iri2 = IRI.from("http://www.perceive.net/schemas/relationship/enemyOf")
                val iri3 = IRI.from("http://example.org/#spiderman")

                val rdfTriple1 = RdfTriple(iri1, iri2, iri3)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1)
                )
            }
            //TODO()
            should("transform example9.n3 without exception") {
                val file = File("src/test/resources/turtle/example9.n3")

                val iri1 = IRI.from("http://one.example/subject1")
                val iri2 = IRI.from("http://one.example/predicate1")
                val iri3 = IRI.from("http://one.example/object1")
                val iri4 = IRI.from("http://one.example/subject2")
                val iri5 = IRI.from("http://one.example/predicate2")
                val iri6 = IRI.from("http://one.example/object2")
                val iri7 = IRI.from("http://two.example/subject3")
                val iri8 = IRI.from("http://two.example/predicate3")
                val iri9 = IRI.from("http://two.example/object3")
                val iri10 = IRI.from("http://one.example/path/subject4")
                val iri11 = IRI.from("http://one.example/path/predicate4")
                val iri12 = IRI.from("http://one.example/path/object4")
                val iri13 = IRI.from("http://another.example/subject5")
                val iri14 = IRI.from("http://another.example/predicate5")
                val iri15 = IRI.from("http://another.example/object5")
                val iri16 = IRI.from("http://another.example/subject6")
                val iri17 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
                val iri18 = IRI.from("http://another.example/subject7")
                val iri19 = IRI.from("http://伝言.example/?user=أكرم&amp;channel=R%26D")
                val iri20 = IRI.from("http://another.example/subject8")


                val rdfTriple1 = RdfTriple(iri1, iri2, iri3)
                val rdfTriple2 = RdfTriple(iri4, iri5, iri6)
                val rdfTriple3 = RdfTriple(iri4, iri5, iri6)
                val rdfTriple4 = RdfTriple(iri7, iri8, iri9)
                val rdfTriple5 = RdfTriple(iri7, iri8, iri9)
                val rdfTriple6 = RdfTriple(iri10, iri11, iri12)
                val rdfTriple7 = RdfTriple(iri13, iri14, iri15)
                val rdfTriple8 = RdfTriple(iri16, iri17, iri18)
                val rdfTriple9 = RdfTriple(iri19, iri17, iri20)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(
                        rdfTriple1,
                        rdfTriple2,
                        rdfTriple3,
                        rdfTriple4,
                        rdfTriple5,
                        rdfTriple6,
                        rdfTriple7,
                        rdfTriple8,
                        rdfTriple9
                    )
                )
            }

            should("transform example10.n3 without exception") {
                val file = File("src/test/resources/turtle/example10.n3")

                val iri1 = IRI.from("http://example.org/#green-goblin")
                val iri2 = IRI.from("http://xmlns.com/foaf/0.1/name")
                val iri3 = IRI.from("http://example.org/#spiderman")

                val literal1 = Literal("Green Goblin", XSDDatatype.XSDstring)
                val literal2 = Literal("Spiderman", XSDDatatype.XSDstring)

                val rdfTriple1 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple2 = RdfTriple(iri3, iri2, literal2)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, rdfTriple2)
                )
            }
            should("transform example11.n3 without exception") {
                val file = File("src/test/resources/turtle/example11.n3")

                val iri1 = IRI.from("http://example.org/vocab/show/218")
                val iri2 = IRI.from("http://www.w3.org/2000/01/rdf-schema#label")
                val iri3 = IRI.from("http://example.org/vocab/show/localName")
                val iri4 = IRI.from("http://example.org/vocab/show/blurb")

                val literal1 = Literal("That Seventies Show", XSDDatatype.XSDstring)
                val literal2 = LanguageTaggedString("That Seventies Show" to "en")
                val literal3 = LanguageTaggedString("Cette Série des Années Soixante-dix" to "fr")
                val literal4 = LanguageTaggedString("Cette Série des Années Septante" to "fr-be")
                val literal5 = Literal(
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


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, rdfTriple2, rdfTriple3, rdfTriple4, rdfTriple5, rdfTriple6, rdfTriple7)
                )
            }
            should("transform example12.n3 without exception") {
                val file = File("src/test/resources/turtle/example12.n3")

                val iri1 = IRI.from("http://en.wikipedia.org/wiki/Helium")
                val iri2 = IRI.from("http://example.org/elementsatomicNumber")
                val iri3 = IRI.from("http://example.org/elementsatomicMass")
                val iri4 = IRI.from("http://example.org/elementsspecificGravity")

                val literal1 = Literal(2, XSDDatatype.XSDinteger)
                val literal2 = Literal(XSDDatatype.XSDdecimal.parse("4.002602"), XSDDatatype.XSDdecimal)
                val literal3 = Literal(1.663E-4, XSDDatatype.XSDdouble)

                val rdfTriple1 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple2 = RdfTriple(iri1, iri3, literal2)
                val rdfTriple3 = RdfTriple(iri1, iri4, literal3)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, rdfTriple2, rdfTriple3)
                )

            }
            should("transform example13.n3 without exception") {
                val file = File("src/test/resources/turtle/example13.n3")

                val iri1 = IRI.from("http://somecountry.example/census2007")
                val iri2 = IRI.from("http://example.org/statsisLandlocked")

                val literal1 = Literal(false, XSDDatatype.XSDboolean)

                val rdfTriple1 = RdfTriple(iri1, iri2, literal1)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1)
                )

            }
            should("transform example14.n3 without exception") {
                val file = File("src/test/resources/turtle/example14.n3")

                val iri1 = IRI.from("http://xmlns.com/foaf/0.1/knows")
                val bn1 = BlankNode("alice")
                val bn2 = BlankNode("bob")


                val rdfTriple1 = RdfTriple(bn1, iri1, bn2)
                val rdfTriple2 = RdfTriple(bn2, iri1, bn1)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(
                        bn1,
                        bn2
                    ), listOf(rdfTriple1, rdfTriple2)
                )
            }
            should("transform example15.n3 without exception") {
                val file = File("src/test/resources/turtle/example15.n3")

                val iri1 = IRI.from("http://xmlns.com/foaf/0.1/knows")
                val iri2 = IRI.from("http://xmlns.com/foaf/0.1/name")


                val bn1 = BlankNode("BN_1")
                val bn2 = BlankNode("BN_2")

                val literal = Literal("Bob", XSDDatatype.XSDstring)


                val rdfTriple1 = RdfTriple(bn1, iri1, bn2)
                val rdfTriple2 = RdfTriple(bn2, iri2, literal)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(
                        bn1,
                        bn2
                    ), listOf(rdfTriple1, rdfTriple2)
                )
            }
            should("transform example16.n3 without exception") {
                val file = File("src/test/resources/turtle/example16.n3")

                val iri1 = IRI.from("http://xmlns.com/foaf/0.1/knows")
                val iri2 = IRI.from("http://xmlns.com/foaf/0.1/name")
                val iri3 = IRI.from("http://xmlns.com/foaf/0.1/mbox")
                val iri4 = IRI.from("bob@example.com")

                val bn1 = BlankNode("BN_1")
                val bn2 = BlankNode("BN_2")
                val bn3 = BlankNode("BN_3")


                val literal1 = Literal("Alice", XSDDatatype.XSDstring)
                val literal2 = Literal("Bob", XSDDatatype.XSDstring)
                val literal3 = Literal("Eve", XSDDatatype.XSDstring)


                val rdfTriple1 = RdfTriple(bn1, iri1, bn3)
                val rdfTriple2 = RdfTriple(bn1, iri2, literal1)
                val rdfTriple3 = RdfTriple(bn2, iri2, literal3)
                val rdfTriple4 = RdfTriple(bn3, iri2, literal2)
                val rdfTriple5 = RdfTriple(bn3, iri1, bn2)
                val rdfTriple6 = RdfTriple(bn3, iri3, iri4)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(
                        bn1,
                        bn2,
                        bn3
                    ), listOf(rdfTriple1, rdfTriple2, rdfTriple3, rdfTriple4, rdfTriple5, rdfTriple6)
                )
            }
            should("transform example17.n3 without exception") {
                val file = File("src/test/resources/turtle/example17.n3")

                val iri1 = IRI.from("http://xmlns.com/foaf/0.1/knows")
                val iri2 = IRI.from("http://xmlns.com/foaf/0.1/name")
                val iri3 = IRI.from("http://xmlns.com/foaf/0.1/mbox")
                val iri4 = IRI.from("bob@example.com")

                val bn1 = BlankNode("a")
                val bn2 = BlankNode("b")
                val bn3 = BlankNode("c")


                val literal1 = Literal("Alice", XSDDatatype.XSDstring)
                val literal2 = Literal("Bob", XSDDatatype.XSDstring)
                val literal3 = Literal("Eve", XSDDatatype.XSDstring)


                val rdfTriple1 = RdfTriple(bn1, iri2, literal1)
                val rdfTriple2 = RdfTriple(bn1, iri1, bn2)
                val rdfTriple3 = RdfTriple(bn2, iri2, literal2)
                val rdfTriple4 = RdfTriple(bn2, iri1, bn3)
                val rdfTriple5 = RdfTriple(bn3, iri2, literal3)
                val rdfTriple6 = RdfTriple(bn2, iri3, iri4)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(
                        bn1,
                        bn2,
                        bn3
                    ), listOf(rdfTriple1, rdfTriple2, rdfTriple3, rdfTriple4, rdfTriple5, rdfTriple6)
                )

            }
            should("transform example18.n3 without exception") {
                val file = File("src/test/resources/turtle/example18.n3")

                val iri1 = IRI.from("http://example.org/foopredicate")
                val iri2 = IRI.from("http://example.org/fooa")
                val iri3 = IRI.from("http://example.org/foob")
                val iri4 = IRI.from("http://example.org/fooc")

                val iri5 = IRI.from("http://example.org/foosubject")
                val iri6 = IRI.from("http://example.org/foopredicate2")

                val bn1 = BlankNode("t")

                val collection1 = Collection(listOf(iri2, iri3, iri4))
                val collection2 = Collection(listOf())


                val rdfTriple1 = RdfTriple(bn1, iri1, collection1)
                val rdfTriple2 = RdfTriple(iri5, iri6, collection2)

                val positiveSurface2 = PositiveSurface(listOf(bn1), listOf(rdfTriple2))

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(bn1), listOf(rdfTriple1, positiveSurface2)
                )
            }
            should("transform example20.n3 without exception") {
                val file = File("src/test/resources/turtle/example20.n3")

                val iri1 = IRI.from("http://example.org/stuff/1.0/a")
                val iri2 = IRI.from("http://example.org/stuff/1.0/b")

                val literal1 = Literal("apple", XSDDatatype.XSDstring)
                val literal2 = Literal("banana", XSDDatatype.XSDstring)

                val collection1 = Collection(listOf(literal1, literal2))

                val rdfTriple1 = RdfTriple(iri1, iri2, collection1)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(), listOf(rdfTriple1)
                )

            }
            should("transform example21.n3 without exception") {
                val file = File("src/test/resources/turtle/example21.n3")

                val iri1 = IRI.from("http://example.org/stuff/1.0/a")
                val iri2 = IRI.from("http://example.org/stuff/1.0/b")
                val iri3 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#first")
                val iri4 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest")
                val iri5 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")

                val bn1 = BlankNode("BN_1")
                val bn2 = BlankNode("BN_2")

                val literal1 = Literal("apple", XSDDatatype.XSDstring)
                val literal2 = Literal("banana", XSDDatatype.XSDstring)


                val rdfTriple1 = RdfTriple(iri1, iri2, bn2)
                val rdfTriple2 = RdfTriple(bn1, iri3, literal2)
                val rdfTriple3 = RdfTriple(bn1, iri4, iri5)
                val rdfTriple4 = RdfTriple(bn2, iri3, literal1)
                val rdfTriple5 = RdfTriple(bn2, iri4, bn1)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(bn1, bn2), listOf(rdfTriple1, rdfTriple2, rdfTriple3, rdfTriple4, rdfTriple5)
                )
            }
            should("transform example22.n3 without exception") {
                val file = File("src/test/resources/turtle/example22.n3")

                val iri1 = IRI.from("http://example.org/stuff/1.0/a")
                val iri2 = IRI.from("http://example.org/stuff/1.0/b")

                val literal1 = Literal("The first line\nThe second line\n  more", XSDDatatype.XSDstring)


                val rdfTriple1 = RdfTriple(iri1, iri2, literal1)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(), listOf(rdfTriple1, rdfTriple1)
                )
            }
            should("transform example23.n3 without exception") {
                val file = File("src/test/resources/turtle/example23.n3")

                val iri1 = IRI.from("http://example.org/stuff/1.0/p")

                val literal1 = Literal("w", XSDDatatype.XSDstring)
                val literal2 = Literal(1, XSDDatatype.XSDinteger)
                val literal3 = Literal(XSDDatatype.XSDdecimal.parse("2.0"), XSDDatatype.XSDdecimal)
                val literal4 = Literal(3E1, XSDDatatype.XSDdouble)


                val collection1 = Collection(listOf(literal2, literal3, literal4))

                val rdfTriple1 = RdfTriple(collection1, iri1, literal1)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(), listOf(rdfTriple1)
                )
            }
            should("transform example24.n3 without exception") {
                val file = File("src/test/resources/turtle/example24.n3")

                val iri1 = IRI.from("http://example.org/stuff/1.0/p")
                val iri2 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#first")
                val iri3 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest")
                val iri4 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")

                val bn0 = BlankNode("b0")
                val bn1 = BlankNode("b1")
                val bn2 = BlankNode("b2")

                val literal1 = Literal("w", XSDDatatype.XSDstring)
                val literal2 = Literal(1, XSDDatatype.XSDinteger)
                val literal3 = Literal(XSDDatatype.XSDdecimal.parse("2.0"), XSDDatatype.XSDdecimal)
                val literal4 = Literal(3E1, XSDDatatype.XSDdouble)

                val rdfTriple1 = RdfTriple(bn0, iri2, literal2)
                val rdfTriple2 = RdfTriple(bn0, iri3, bn1)
                val rdfTriple3 = RdfTriple(bn1, iri2, literal3)
                val rdfTriple4 = RdfTriple(bn1, iri3, bn2)
                val rdfTriple5 = RdfTriple(bn2, iri2, literal4)
                val rdfTriple6 = RdfTriple(bn2, iri3, iri4)
                val rdfTriple7 = RdfTriple(bn0, iri1, literal1)

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(bn0, bn1, bn2),
                    listOf(rdfTriple1, rdfTriple2, rdfTriple3, rdfTriple4, rdfTriple5, rdfTriple6, rdfTriple7)
                )
            }
            should("transform example25.n3 without exception") {
                val file = File("src/test/resources/turtle/example25.n3")

                val iri1 = IRI.from("http://example.org/stuff/1.0/p")
                val iri2 = IRI.from("http://example.org/stuff/1.0/q")
                val iri3 = IRI.from("http://example.org/stuff/1.0/p2")
                val iri4 = IRI.from("http://example.org/stuff/1.0/q2")


                val bn0 = BlankNode("BN_1")

                val literal1 = Literal(1, XSDDatatype.XSDinteger)
                val literal2 = Literal(2, XSDDatatype.XSDinteger)

                val collection1 = Collection(listOf(literal2))
                val collection2 = Collection(listOf(literal1, bn0, collection1))

                val rdfTriple1 = RdfTriple(collection2, iri3, iri4)
                val rdfTriple2 = RdfTriple(bn0, iri1, iri2)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(bn0),
                    listOf(rdfTriple1, rdfTriple2)
                )
            }
            should("transform example26.n3 without exception") {
                val file = File("src/test/resources/turtle/example26.n3")


                val iri1 = IRI.from("http://example.org/stuff/1.0/p")
                val iri2 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#first")
                val iri3 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest")
                val iri4 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                val iri5 = IRI.from("http://example.org/stuff/1.0/q")


                val bn0 = BlankNode("b0")
                val bn1 = BlankNode("b1")
                val bn2 = BlankNode("b2")
                val bn3 = BlankNode("b3")
                val bn4 = BlankNode("b4")


                val literal1 = Literal(1, XSDDatatype.XSDinteger)
                val literal2 = Literal(2, XSDDatatype.XSDinteger)

                val rdfTriple1 = RdfTriple(bn0, iri2, literal1)
                val rdfTriple2 = RdfTriple(bn0, iri3, bn1)
                val rdfTriple3 = RdfTriple(bn1, iri2, bn2)
                val rdfTriple4 = RdfTriple(bn2, iri1, iri5)
                val rdfTriple5 = RdfTriple(bn1, iri3, bn3)
                val rdfTriple6 = RdfTriple(bn3, iri2, bn4)
                val rdfTriple7 = RdfTriple(bn4, iri2, literal2)
                val rdfTriple8 = RdfTriple(bn4, iri3, iri4)
                val rdfTriple9 = RdfTriple(bn3, iri3, iri4)


                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(bn0, bn1, bn2, bn3, bn4),
                    listOf(
                        rdfTriple1,
                        rdfTriple2,
                        rdfTriple3,
                        rdfTriple4,
                        rdfTriple5,
                        rdfTriple6,
                        rdfTriple7,
                        rdfTriple8,
                        rdfTriple9,
                    )
                )
            }

        }

        context("blogic") {
            should("transform blogic abc.n3") {
                val file = File("src/test/resources/blogic/abc.n3")

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

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, negativeSurface1, negativeSurface2, querySurface)
                )
            }
            should("transform blogic abcd.n3") {
                val file = File("src/test/resources/blogic/abcd.n3")

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

                RDFSurfacesParser(false).parseToEnd(file.readText()) shouldBeEqualToComparingFields PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, negativeSurface1, negativeSurface2, negativeSurface3, querySurface)
                )
            }
        }
        context("duplicated Blank Nodes") {
            should("change Blank Nodes Base") {
                val testStr = "@prefix : <http://example.org#> . _:BN_12 _:BN_043 _:BN_0023. (:a _:BN_23 []) :b :c."
                val rdfSurfaceParser = RDFSurfacesParser(true)

                rdfSurfaceParser.parseToEnd(testStr).graffiti.size shouldBe 8
            }
            should("throw an exeption") {
                val testStr = "@prefix : <http://example.org#> . _:BN_12 _:BN_043 _:BN_0000000000000023. (:a _:BN_23 []) :b :c."
                val rdfSurfaceParser = RDFSurfacesParser(true)
                shouldThrow<RDFSurfacesParseException> { rdfSurfaceParser.parseToEnd(testStr) }
            }

        }
        context("case insensitive base") {
            should("accept case insensitive 'base' and 'prefix'") {
                val testStr = "BaSe <http://example.org#> \n prEfix : <http://example.de#> \n  <#prefix> :a \"base\""
                val rdfSurfaceParser = RDFSurfacesParser(false)

                val iri1 = IRI.from("http://example.org#prefix")
                val iri2 = IRI.from("http://example.de#a")

                val literal = Literal.fromNonNumericLiteral("base", IRI.from(IRIConstants.XSD_STRING_IRI))

                rdfSurfaceParser.parseToEnd(testStr) shouldBeEqualToComparingFields PositiveSurface(listOf(), listOf(RdfTriple(iri1,iri2,literal)))
            }
        }

    })
