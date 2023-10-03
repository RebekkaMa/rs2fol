package controllerTest
import controller.Transformer
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import parser.RDFSurfacesParser
import rdfSurfaces.IRI
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.readText

class RDFSurfaceToFOLControllerTest : ShouldSpec(
    {
        val rdfSurfacesParser = RDFSurfacesParser(false)

        should("transform example2.n3 without exception") {
            val file = Path("src/test/resources/turtle/example2.n3")
            val solutionFile = Path("src/test/resources/turtle/example2.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example3.n3 without exception") {
            val file = Path("src/test/resources/turtle/example3.n3")
            val solutionFile = Path("src/test/resources/turtle/example3.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example4.n3 without exception") {
            val file = Path("src/test/resources/turtle/example4.n3")
            val solutionFile = Path("src/test/resources/turtle/example4.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example5.n3 without exception") {
            val file = Path("src/test/resources/turtle/example5.n3")
            val solutionFile = Path("src/test/resources/turtle/example5.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example6.n3 without exception") {
            val file = Path("src/test/resources/turtle/example6.n3")
            val solutionFile = Path("src/test/resources/turtle/example6.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example7.n3 without exception") {
            val file = Path("src/test/resources/turtle/example7.n3")
            val solutionFile = Path("src/test/resources/turtle/example7.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example8.n3 without exception") {
            val file = Path("src/test/resources/turtle/example8.n3")
            val solutionFile = Path("src/test/resources/turtle/example8.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example9.n3 without exception") {
            val file = Path("src/test/resources/turtle/example9.n3")
            val solutionFile = Path("src/test/resources/turtle/example9.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example10.n3 without exception") {
            val file = Path("src/test/resources/turtle/example10.n3")
            val solutionFile = Path("src/test/resources/turtle/example10.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example11.n3 without exception") {
            val file = Path("src/test/resources/turtle/example11.n3")
            val solutionFile = Path("src/test/resources/turtle/example11.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example12.n3 without exception") {
            val file = Path("src/test/resources/turtle/example12.n3")
            val solutionFile = Path("src/test/resources/turtle/example12.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example13.n3 without exception") {
            val file = Path("src/test/resources/turtle/example13.n3")
            val solutionFile = Path("src/test/resources/turtle/example13.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example14.n3 without exception") {
            val file = Path("src/test/resources/turtle/example14.n3")
            val solutionFile = Path("src/test/resources/turtle/example14.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example15.n3 without exception") {
            val file = Path("src/test/resources/turtle/example15.n3")
            val solutionFile = Path("src/test/resources/turtle/example15.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example16.n3 without exception") {
            val file = Path("src/test/resources/turtle/example16.n3")

            val solution = """fof(axiom,axiom,
                ? [BN_1,BN_2,BN_3] : (
            triple(BN_1,'http://xmlns.com/foaf/0.1/knows',BN_3)
            & triple(BN_1,'http://xmlns.com/foaf/0.1/name','"Alice"^^http://www.w3.org/2001/XMLSchema#string')
            & triple(BN_2,'http://xmlns.com/foaf/0.1/name','"Eve"^^http://www.w3.org/2001/XMLSchema#string')
            & triple(BN_3,'http://xmlns.com/foaf/0.1/name','"Bob"^^http://www.w3.org/2001/XMLSchema#string')
            & triple(BN_3,'http://xmlns.com/foaf/0.1/knows',BN_2)
            & triple(BN_3,'http://xmlns.com/foaf/0.1/mbox','file://${file.absolute().parent.invariantSeparatorsPathString}/bob@example.com')
            )
            )."""

            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solution.replace("\\s".toRegex(), "")
        }
        should("transform example17.n3 without exception") {
            val file = Path("src/test/resources/turtle/example17.n3")
            val solution = """fof(axiom,axiom,
   ? [Ox0061,Ox0062,Ox0063] : (
      triple(Ox0061,'http://xmlns.com/foaf/0.1/name','"Alice"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(Ox0061,'http://xmlns.com/foaf/0.1/knows',Ox0062)
      & triple(Ox0062,'http://xmlns.com/foaf/0.1/name','"Bob"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(Ox0062,'http://xmlns.com/foaf/0.1/knows',Ox0063)
      & triple(Ox0063,'http://xmlns.com/foaf/0.1/name','"Eve"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(Ox0062,'http://xmlns.com/foaf/0.1/mbox','file://${file.absolute().parent.invariantSeparatorsPathString}/bob@example.com')
   )
)."""

            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solution.replace("\\s".toRegex(), "")
        }
        should("transform example20.n3 without exception") {
            val file = Path("src/test/resources/turtle/example20.n3")
            val solutionFile = Path("src/test/resources/turtle/example20.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example21.n3 without exception") {
            val file = Path("src/test/resources/turtle/example21.n3")
            val solutionFile = Path("src/test/resources/turtle/example21.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")

        }
        should("transform example22.n3 without exception") {
            val file = Path("src/test/resources/turtle/example22.n3")
            val solutionFile = Path("src/test/resources/turtle/example22.p")

            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example23.n3 without exception") {
            val file = Path("src/test/resources/turtle/example23.n3")
            val solutionFile = Path("src/test/resources/turtle/example23.p")

            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example24.n3 without exception") {
            val file = Path("src/test/resources/turtle/example24.n3")
            val solutionFile = Path("src/test/resources/turtle/example24.p")
            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example25.n3 without exception") {
            val file = Path("src/test/resources/turtle/example25.n3")
            val solutionFile = Path("src/test/resources/turtle/example25.p")

            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example26.n3 without exception") {
            val file = Path("src/test/resources/turtle/example26.n3")
            val solutionFile = Path("src/test/resources/turtle/example26.p")

            (Transformer().toFOL(
                rdfSurfacesParser.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                )
            )).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }


        context("blogic") {
            should("transform blogic abc.n3s") {
                val file = Path("src/test/resources/blogic/abc.n3s")
                val solutionFile = Path("src/test/resources/blogic/abc.p")

                Transformer().toFOL(
                    rdfSurfacesParser.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    )
                ).replace("\\s+".toRegex(), "") shouldBeEqualComparingTo solutionFile.readText().replace("\\s+".toRegex(), "")
            }
            should("transform blogic abcd.n3s") {
                val file = Path("src/test/resources/blogic/abcd.n3s")
                val solutionFile = Path("src/test/resources/blogic/abcd.p")

                Transformer().toFOL(
                    rdfSurfacesParser.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    )
                ).replace("\\s+".toRegex(), "") shouldBeEqualComparingTo solutionFile.readText().replace("\\s+".toRegex(), "")
            }
        }
        context("lists") {
            should("transform lists.n3 without exception") {
                val file = Path("src/test/resources/turtle/lists.n3")
                val solutionFile = Path("src/test/resources/turtle/lists.p")
                (Transformer().toFOL(
                    rdfSurfacesParser.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    )
                )).replace(
                    "\\s".toRegex(),
                    ""
                ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
            }
            should("transform lists.n3 without exception (with RDF first-rest chains)") {
                val file = Path("src/test/resources/turtle/lists.n3")
                val solutionFile = Path("src/test/resources/turtle/lists-rdf.p")
                (Transformer().toFOL(
                    RDFSurfacesParser(useRDFLists = true).parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    )
                )).replace(
                    "\\s".toRegex(),
                    ""
                ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
            }
        }
    })
