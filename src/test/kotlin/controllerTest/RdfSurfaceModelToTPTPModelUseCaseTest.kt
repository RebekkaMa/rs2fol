package controllerTest

import entities.fol.FOLAnd
import entities.fol.FOLConstant
import entities.fol.FOLPredicate
import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.parser.RDFSurfaceParseService
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe
import use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import util.commandResult.getSuccessOrNull
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.readText

class RdfSurfaceModelToTPTPModelUseCaseTest : ShouldSpec(
    {
        val rdfSurfaceParseService = RDFSurfaceParseService(false)

        should("transform example2.n3 without exception") {
            val file = Path("src/test/resources/turtle/example2.n3")
            val solutionFile = Path("src/test/resources/turtle/example2.p")
            val result = RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLPredicate(
                    "triple",
                    listOf(
                        FOLConstant("http://example.org/#spiderman"),
                        FOLConstant("http://www.perceive.net/schemas/relationship/enemyOf"),
                        FOLConstant("http://example.org/#green-goblin")
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example3.n3 without exception") {
            val file = Path("src/test/resources/turtle/example3.n3")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/#spiderman"),
                                FOLConstant("http://www.perceive.net/schemas/relationship/enemyOf"),
                                FOLConstant("http://example.org/#green-goblin")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/#spiderman"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Spiderman\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }


        should("transform example4.n3 without exception") {
            val file = Path("src/test/resources/turtle/example4.n3")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/#spiderman"),
                                FOLConstant("http://www.perceive.net/schemas/relationship/enemyOf"),
                                FOLConstant("http://example.org/#green-goblin")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/#spiderman"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Spiderman\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }
        should("transform example5.n3 without exception") {
            val file = Path("src/test/resources/turtle/example5.n3")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/#spiderman"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Spiderman\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/#spiderman"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Человек-паук\"@ru")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example6.n3 without exception") {
            val file = Path("src/test/resources/turtle/example6.n3")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/#spiderman"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Spiderman\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/#spiderman"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Человек-паук\"@ru")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example7.n3 without exception") {
            val file = Path("src/test/resources/turtle/example7.n3")
            val solutionFile = Path("src/test/resources/turtle/example7.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLPredicate(
                    "triple",
                    listOf(
                        FOLConstant("http://example.org/#green-goblin"),
                        FOLConstant("http://www.perceive.net/schemas/relationship/enemyOf"),
                        FOLConstant("http://example.org/#spiderman")
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example8.n3 without exception") {
            val file = Path("src/test/resources/turtle/example8.n3")
            val solutionFile = Path("src/test/resources/turtle/example8.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLPredicate(
                    "triple",
                    listOf(
                        FOLConstant("http://example.org/#green-goblin"),
                        FOLConstant("http://www.perceive.net/schemas/relationship/enemyOf"),
                        FOLConstant("http://example.org/#spiderman")
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example9.n3 without exception") {
            val file = Path("src/test/resources/turtle/example9.n3")
            val solutionFile = Path("src/test/resources/turtle/example9.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://one.example/subject1"),
                                FOLConstant("http://one.example/predicate1"),
                                FOLConstant("http://one.example/object1")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://one.example/subject2"),
                                FOLConstant("http://one.example/predicate2"),
                                FOLConstant("http://one.example/object2")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://one.example/subject2"),
                                FOLConstant("http://one.example/predicate2"),
                                FOLConstant("http://one.example/object2")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://two.example/subject3"),
                                FOLConstant("http://two.example/predicate3"),
                                FOLConstant("http://two.example/object3")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://two.example/subject3"),
                                FOLConstant("http://two.example/predicate3"),
                                FOLConstant("http://two.example/object3")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://one.example/path/subject4"),
                                FOLConstant("http://one.example/path/predicate4"),
                                FOLConstant("http://one.example/path/object4")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://another.example/subject5"),
                                FOLConstant("http://another.example/predicate5"),
                                FOLConstant("http://another.example/object5")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://another.example/subject6"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                FOLConstant("http://another.example/subject7")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://伝言.example/?user=أكرم&amp;channel=R%26D"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                FOLConstant("http://another.example/subject8")
                            )
                        ),
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example10.n3 without exception") {
            val file = Path("src/test/resources/turtle/example10.n3")
            val solutionFile = Path("src/test/resources/turtle/example10.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result?.joinToString(System.lineSeparator()).shouldNotBeNull().replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }

        should("transform example11.n3 without exception") {
            val file = Path("src/test/resources/turtle/example11.n3")
            val solutionFile = Path("src/test/resources/turtle/example11.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example12.n3 without exception") {
            val file = Path("src/test/resources/turtle/example12.n3")
            val solutionFile = Path("src/test/resources/turtle/example12.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()

            result.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example13.n3 without exception") {
            val file = Path("src/test/resources/turtle/example13.n3")
            val solutionFile = Path("src/test/resources/turtle/example13.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()

            result!!.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example14.n3 without exception") {
            val file = Path("src/test/resources/turtle/example14.n3")
            val solutionFile = Path("src/test/resources/turtle/example14.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example15.n3 without exception") {
            val file = Path("src/test/resources/turtle/example15.n3")
            val solutionFile = Path("src/test/resources/turtle/example15.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
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

            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
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

            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solution.replace("\\s".toRegex(), "")
        }
        should("transform example20.n3 without exception") {
            val file = Path("src/test/resources/turtle/example20.n3")
            val solutionFile = Path("src/test/resources/turtle/example20.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example21.n3 without exception") {
            val file = Path("src/test/resources/turtle/example21.n3")
            val solutionFile = Path("src/test/resources/turtle/example21.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")

        }
        should("transform example22.n3 without exception") {
            val file = Path("src/test/resources/turtle/example22.n3")
            val solutionFile = Path("src/test/resources/turtle/example22.p")

            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()?.joinToString(System.lineSeparator())
                .shouldNotBeNull() shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example23.n3 without exception") {
            val file = Path("src/test/resources/turtle/example23.n3")
            val solutionFile = Path("src/test/resources/turtle/example23.p")

            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example24.n3 without exception") {
            val file = Path("src/test/resources/turtle/example24.n3")
            val solutionFile = Path("src/test/resources/turtle/example24.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example25.n3 without exception") {
            val file = Path("src/test/resources/turtle/example25.n3")
            val solutionFile = Path("src/test/resources/turtle/example25.p")

            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull()
            result shouldNotBe null

            result!!.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }
        should("transform example26.n3 without exception") {
            val file = Path("src/test/resources/turtle/example26.n3")
            val solutionFile = Path("src/test/resources/turtle/example26.p")

            val result = (RdfSurfaceModelToTPTPModelUseCase(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                ).getSuccessOrNull().shouldNotBeNull()
            )).getSuccessOrNull().shouldNotBeNull()
            result.joinToString(System.lineSeparator()).replace(
                "\\s".toRegex(),
                ""
            ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
        }


        context("blogic") {
            should("transform blogic abc.n3s") {
                val file = Path("src/test/resources/blogic/abc.n3s")
                val solutionFile = Path("src/test/resources/blogic/abc.p")

                val result = RdfSurfaceModelToTPTPModelUseCase(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    ).getSuccessOrNull().shouldNotBeNull()
                ).getSuccessOrNull().shouldNotBeNull()
                result.joinToString(System.lineSeparator())
                    .replace("\\s+".toRegex(), "") shouldBeEqualComparingTo solutionFile.readText()
                    .replace("\\s+".toRegex(), "")
            }
            should("transform blogic abcd.n3s") {
                val file = Path("src/test/resources/blogic/abcd.n3s")
                val solutionFile = Path("src/test/resources/blogic/abcd.p")

                RdfSurfaceModelToTPTPModelUseCase(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    ).getSuccessOrNull().shouldNotBeNull()
                ).getSuccessOrNull().shouldNotBeNull().joinToString(System.lineSeparator())
                    .replace("\\s+".toRegex(), "") shouldBeEqualComparingTo solutionFile.readText()
                    .replace("\\s+".toRegex(), "")
            }
        }
        context("lists") {
            should("transform lists.n3 without exception") {
                val file = Path("src/test/resources/turtle/lists.n3")
                val solutionFile = Path("src/test/resources/turtle/lists.p")
                val result = (RdfSurfaceModelToTPTPModelUseCase(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    ).getSuccessOrNull().shouldNotBeNull()
                )).getSuccessOrNull()
                result shouldNotBe null
                result!!.joinToString(System.lineSeparator()).replace(
                    "\\s".toRegex(),
                    ""
                ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
            }
            should("transform lists.n3 without exception (with RDF first-rest chains)") {
                val file = Path("src/test/resources/turtle/lists.n3")
                val solutionFile = Path("src/test/resources/turtle/lists-rdf.p")
                val result = RdfSurfaceModelToTPTPModelUseCase(
                    RDFSurfaceParseService(useRDFLists = true).parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/")
                    ).getSuccessOrNull().shouldNotBeNull()
                ).getSuccessOrNull().shouldNotBeNull().joinToString(System.lineSeparator()).replace(
                    "\\s".toRegex(),
                    ""
                ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
            }
        }
    }
)
