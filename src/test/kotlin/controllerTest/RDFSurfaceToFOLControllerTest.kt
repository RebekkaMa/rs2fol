package controllerTest

import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.parsing.RDFSurfaceParseService
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe
import use_cases.modelTransformer.RdfSurfaceModelToFolUseCase
import util.error.getSuccessOrNull
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.readText

class RDFSurfaceToFOLControllerTest : ShouldSpec(
    {
        val rdfSurfaceParseService = RDFSurfaceParseService(false)

        should("transform example2.n3 without exception") {
            val file = Path("src/test/resources/turtle/example2.n3")
            val solutionFile = Path("src/test/resources/turtle/example2.p")
            val result = RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            ).getSuccessOrNull()
            result shouldNotBe null
            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example3.n3 without exception") {
            val file = Path("src/test/resources/turtle/example3.n3")
            val solutionFile = Path("src/test/resources/turtle/example3.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()

            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example4.n3 without exception") {
            val file = Path("src/test/resources/turtle/example4.n3")
            val solutionFile = Path("src/test/resources/turtle/example4.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result shouldNotBe null
            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example5.n3 without exception") {
            val file = Path("src/test/resources/turtle/example5.n3")
            val solutionFile = Path("src/test/resources/turtle/example5.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result shouldNotBe null

            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example6.n3 without exception") {
            val file = Path("src/test/resources/turtle/example6.n3")
            val solutionFile = Path("src/test/resources/turtle/example6.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result shouldNotBe null

            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example7.n3 without exception") {
            val file = Path("src/test/resources/turtle/example7.n3")
            val solutionFile = Path("src/test/resources/turtle/example7.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result shouldNotBe null

            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example8.n3 without exception") {
            val file = Path("src/test/resources/turtle/example8.n3")
            val solutionFile = Path("src/test/resources/turtle/example8.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result shouldNotBe null

            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example9.n3 without exception") {
            val file = Path("src/test/resources/turtle/example9.n3")
            val solutionFile = Path("src/test/resources/turtle/example9.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example10.n3 without exception") {
            val file = Path("src/test/resources/turtle/example10.n3")
            val solutionFile = Path("src/test/resources/turtle/example10.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result.shouldNotBeNull().replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example11.n3 without exception") {
            val file = Path("src/test/resources/turtle/example11.n3")
            val solutionFile = Path("src/test/resources/turtle/example11.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example12.n3 without exception") {
            val file = Path("src/test/resources/turtle/example12.n3")
            val solutionFile = Path("src/test/resources/turtle/example12.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()

            result.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example13.n3 without exception") {
            val file = Path("src/test/resources/turtle/example13.n3")
            val solutionFile = Path("src/test/resources/turtle/example13.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()

            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example14.n3 without exception") {
            val file = Path("src/test/resources/turtle/example14.n3")
            val solutionFile = Path("src/test/resources/turtle/example14.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example15.n3 without exception") {
            val file = Path("src/test/resources/turtle/example15.n3")
            val solutionFile = Path("src/test/resources/turtle/example15.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
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

            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
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

            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solution.replace("\\s".toRegex(), "")
        }
        should("transform example20.n3 without exception") {
            val file = Path("src/test/resources/turtle/example20.n3")
            val solutionFile = Path("src/test/resources/turtle/example20.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example21.n3 without exception") {
            val file = Path("src/test/resources/turtle/example21.n3")
            val solutionFile = Path("src/test/resources/turtle/example21.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")

        }
        should("transform example22.n3 without exception") {
            val file = Path("src/test/resources/turtle/example22.n3")
            val solutionFile = Path("src/test/resources/turtle/example22.p")

            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull() shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example23.n3 without exception") {
            val file = Path("src/test/resources/turtle/example23.n3")
            val solutionFile = Path("src/test/resources/turtle/example23.p")

            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example24.n3 without exception") {
            val file = Path("src/test/resources/turtle/example24.n3")
            val solutionFile = Path("src/test/resources/turtle/example24.p")
            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example25.n3 without exception") {
            val file = Path("src/test/resources/turtle/example25.n3")
            val solutionFile = Path("src/test/resources/turtle/example25.p")

            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result shouldNotBe null

            result!!.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example26.n3 without exception") {
            val file = Path("src/test/resources/turtle/example26.n3")
            val solutionFile = Path("src/test/resources/turtle/example26.p")

            val result = (RdfSurfaceModelToFolUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }


        context("blogic") {
            should("transform blogic abc.n3s") {
                val file = Path("src/test/resources/blogic/abc.n3s")
                val solutionFile = Path("src/test/resources/blogic/abc.p")

                val result = RdfSurfaceModelToFolUseCase(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    ).getSuccessOrNull().shouldNotBeNull()
                ).getSuccessOrNull().shouldNotBeNull()
                result.replace("\\s+".toRegex(), "") shouldBeEqualComparingTo solutionFile.readText()
                    .replace("\\s+".toRegex(), "")
            }
            should("transform blogic abcd.n3s") {
                val file = Path("src/test/resources/blogic/abcd.n3s")
                val solutionFile = Path("src/test/resources/blogic/abcd.p")

                RdfSurfaceModelToFolUseCase(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    ).getSuccessOrNull().shouldNotBeNull()
                ).getSuccessOrNull().shouldNotBeNull()
                    .replace("\\s+".toRegex(), "") shouldBeEqualComparingTo solutionFile.readText()
                    .replace("\\s+".toRegex(), "")
            }
        }
        context("lists") {
            should("transform lists.n3 without exception") {
                val file = Path("src/test/resources/turtle/lists.n3")
                val solutionFile = Path("src/test/resources/turtle/lists.p")
                val result = (RdfSurfaceModelToFolUseCase(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    ).getSuccessOrNull().shouldNotBeNull()
                )).getSuccessOrNull()
                result shouldNotBe null
                result!!.replace(
                    "\\s".toRegex(),
                    ""
                ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
            }
            should("transform lists.n3 without exception (with RDF first-rest chains)") {
                val file = Path("src/test/resources/turtle/lists.n3")
                val solutionFile = Path("src/test/resources/turtle/lists-rdf.p")
                val result = RdfSurfaceModelToFolUseCase(
                    RDFSurfaceParseService(useRDFLists = true).parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    ).getSuccessOrNull().shouldNotBeNull()
                ).getSuccessOrNull().shouldNotBeNull().replace(
                    "\\s".toRegex(),
                    ""
                ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
            }
        }
    })
