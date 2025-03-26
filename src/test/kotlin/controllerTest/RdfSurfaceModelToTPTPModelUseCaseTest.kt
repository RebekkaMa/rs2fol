package controllerTest

import adapter.jena.LiteralServiceImpl
import adapter.parser.RDFSurfaceParseServiceImpl
import app.use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import entities.fol.*
import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import entities.rdfsurfaces.rdf_term.IRI
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe
import util.commandResult.getSuccessOrNull
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.readText

class RdfSurfaceModelToTPTPModelUseCaseTest : ShouldSpec(
    {
        val rdfSurfaceParseService = RDFSurfaceParseServiceImpl(
            literalService = LiteralServiceImpl()
        )

        should("transform example2.n3 without exception") {
            val file = Path("src/test/resources/turtle/example2.n3")
            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
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
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
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
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
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
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
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
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
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
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
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
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
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
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
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

            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            )).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/#green-goblin"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Green Goblin\"^^http://www.w3.org/2001/XMLSchema#string")
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

        should("transform example11.n3 without exception") {
            val file = Path("src/test/resources/turtle/example11.n3")

            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()


            val expectedResult =
                AnnotatedFormula(
                    "axiom",
                    FormulaType.Axiom,
                    FOLAnd(
                        listOf(
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLConstant("http://example.org/vocab/show/218"),
                                    FOLConstant("http://www.w3.org/2000/01/rdf-schema#label"),
                                    FOLConstant("\"That Seventies Show\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLConstant("http://example.org/vocab/show/218"),
                                    FOLConstant("http://www.w3.org/2000/01/rdf-schema#label"),
                                    FOLConstant("\"That Seventies Show\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLConstant("http://example.org/vocab/show/218"),
                                    FOLConstant("http://www.w3.org/2000/01/rdf-schema#label"),
                                    FOLConstant("\"That Seventies Show\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLConstant("http://example.org/vocab/show/218"),
                                    FOLConstant("http://example.org/vocab/show/localName"),
                                    FOLConstant("\"That Seventies Show\"@en")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLConstant("http://example.org/vocab/show/218"),
                                    FOLConstant("http://example.org/vocab/show/localName"),
                                    FOLConstant("\"Cette Série des Années Soixante-dix\"@fr")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLConstant("http://example.org/vocab/show/218"),
                                    FOLConstant("http://example.org/vocab/show/localName"),
                                    FOLConstant("\"Cette Série des Années Septante\"@fr-be")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLConstant("http://example.org/vocab/show/218"),
                                    FOLConstant("http://example.org/vocab/show/blurb"),
                                    FOLConstant(
                                        "\"This is a multi-line                        # literal with embedded new lines and quotes\n" +
                                                "literal with many quotes (\"\"\"\"\")\n" +
                                                "and up to two sequential apostrophes ('').\"^^http://www.w3.org/2001/XMLSchema#string"
                                    )
                                )
                            )
                        )
                    )
                );

            { result.toString() + System.lineSeparator() + listOf(expectedResult).toString() }.asClue {
                result shouldNotBe null
                result!! shouldBeEqual listOf(expectedResult)
            }

        }

        should("transform example12.n3 without exception") {
            val file = Path("src/test/resources/turtle/example12.n3")
            val solutionFile = Path("src/test/resources/turtle/example12.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            )).getSuccessOrNull().shouldNotBeNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://en.wikipedia.org/wiki/Helium"),
                                FOLConstant("http://example.org/elementsatomicNumber"),
                                FOLConstant("\"2\"^^http://www.w3.org/2001/XMLSchema#integer")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://en.wikipedia.org/wiki/Helium"),
                                FOLConstant("http://example.org/elementsatomicMass"),
                                FOLConstant("\"4.002602\"^^http://www.w3.org/2001/XMLSchema#decimal")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://en.wikipedia.org/wiki/Helium"),
                                FOLConstant("http://example.org/elementsspecificGravity"),
                                FOLConstant("\"1.663E-4\"^^http://www.w3.org/2001/XMLSchema#double")
                            )
                        )
                    )
                )
            )


            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example13.n3 without exception") {
            val file = Path("src/test/resources/turtle/example13.n3")
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            )).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLPredicate(
                    "triple",
                    listOf(
                        FOLConstant("http://somecountry.example/census2007"),
                        FOLConstant("http://example.org/statsisLandlocked"),
                        FOLConstant("\"false\"^^http://www.w3.org/2001/XMLSchema#boolean")
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example14.n3 without exception") {
            val file = Path("src/test/resources/turtle/example14.n3")
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            )).getSuccessOrNull().shouldNotBeNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLExists(
                    variables = listOf(
                        FOLVariable("alice"),
                        FOLVariable("bob")
                    ),
                    FOLAnd(
                        listOf(
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("alice"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/knows"),
                                    FOLVariable("bob")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("bob"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/knows"),
                                    FOLVariable("alice")
                                )
                            )
                        )
                    )
                )
            );

            { result.toString() + System.lineSeparator() + listOf(expectedResult).toString() }.asClue {
                result shouldNotBe null
                result shouldBeEqual listOf(expectedResult)
            }
        }

        should("transform example15.n3 without exception") {
            val file = Path("src/test/resources/turtle/example15.n3")
            val solutionFile = Path("src/test/resources/turtle/example15.p")
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            )).getSuccessOrNull().shouldNotBeNull()
            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLExists(
                    variables = listOf(
                        FOLVariable("BN_1"),
                        FOLVariable("BN_2")
                    ),
                    FOLAnd(
                        listOf(
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_1"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/knows"),
                                    FOLVariable("BN_2")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_2"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                    FOLConstant("\"Bob\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result shouldBeEqual listOf(expectedResult)
        }
        should("transform example16.n3 without exception") {
            val file = Path("src/test/resources/turtle/example16.n3")
            val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            )).getSuccessOrNull().shouldNotBeNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLExists(
                    variables = listOf(
                        FOLVariable("BN_1"),
                        FOLVariable("BN_2"),
                        FOLVariable("BN_3")
                    ),
                    FOLAnd(
                        listOf(
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_1"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/knows"),
                                    FOLVariable("BN_3")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_1"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                    FOLConstant("\"Alice\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_2"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                    FOLConstant("\"Eve\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_3"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                    FOLConstant("\"Bob\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_3"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/knows"),
                                    FOLVariable("BN_2")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_3"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/mbox"),
                                    FOLConstant("file://" + file.absolute().parent.invariantSeparatorsPathString + "/bob@example.com")
                                )
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result shouldBeEqual listOf(expectedResult)
        }

        should("transform example17.n3 without exception") {
            val file = Path("src/test/resources/turtle/example17.n3")
            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("Ox0061"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Alice\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("Ox0061"),
                                FOLConstant("http://xmlns.com/foaf/0.1/knows"),
                                FOLVariable("Ox0062")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("Ox0062"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Bob\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("Ox0062"),
                                FOLConstant("http://xmlns.com/foaf/0.1/knows"),
                                FOLVariable("Ox0063")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("Ox0063"),
                                FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                FOLConstant("\"Eve\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("Ox0062"),
                                FOLConstant("http://xmlns.com/foaf/0.1/mbox"),
                                FOLConstant("file://" + file.absolute().parent.invariantSeparatorsPathString + "/bob@example.com")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example20.n3 without exception") {
            val file = Path("src/test/resources/turtle/example20.n3")
            val solutionFile = Path("src/test/resources/turtle/example20.p")
            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLPredicate(
                    "triple",
                    listOf(
                        FOLConstant("http://example.org/stuff/1.0/a"),
                        FOLConstant("http://example.org/stuff/1.0/b"),
                        FOLFunction(
                            "list",
                            listOf(
                                FOLConstant("\"apple\"^^http://www.w3.org/2001/XMLSchema#string"),
                                FOLConstant("\"banana\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }
        should("transform example21.n3 without exception") {
            val file = Path("src/test/resources/turtle/example21.n3")
            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/stuff/1.0/a"),
                                FOLConstant("http://example.org/stuff/1.0/b"),
                                FOLVariable("BN_2")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("BN_1"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                FOLConstant("\"banana\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("BN_1"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("BN_2"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                FOLConstant("\"apple\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("BN_2"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                FOLVariable("BN_1")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example22.n3 without exception") {
            val file = Path("src/test/resources/turtle/example22.n3")
            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/stuff/1.0/a"),
                                FOLConstant("http://example.org/stuff/1.0/b"),
                                FOLConstant("\"The first line\\u000AThe second line\\u000A  more\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/stuff/1.0/a"),
                                FOLConstant("http://example.org/stuff/1.0/b"),
                                FOLConstant("\"The first line\\u000AThe second line\\u000A  more\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }
        should("transform example23.n3 without exception") {
            val file = Path("src/test/resources/turtle/example23.n3")
            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLPredicate(
                    "triple",
                    listOf(
                        FOLFunction(
                            "list",
                            listOf(
                                FOLConstant("\"1\"^^http://www.w3.org/2001/XMLSchema#integer"),
                                FOLConstant("\"2\"^^http://www.w3.org/2001/XMLSchema#decimal"),
                                FOLConstant("\"30.0\"^^http://www.w3.org/2001/XMLSchema#double")
                            )
                        ),
                        FOLConstant("http://example.org/stuff/1.0/p"),
                        FOLConstant("\"w\"^^http://www.w3.org/2001/XMLSchema#string")
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }
        should("transform example24.n3 without exception") {
            val file = Path("src/test/resources/turtle/example24.n3")
            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("b0"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                FOLConstant("\"1\"^^http://www.w3.org/2001/XMLSchema#integer")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("b0"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                FOLVariable("Ox00621")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("b1"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                FOLConstant("\"2\"^^http://www.w3.org/2001/XMLSchema#decimal")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("b1"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                FOLVariable("b2")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("b2"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                FOLConstant("\"30.0\"^^http://www.w3.org/2001/XMLSchema#double")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("b2"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("b0"),
                                FOLConstant("http://example.org/stuff/1.0/p"),
                                FOLConstant("\"w\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example25.n3 without exception") {
            val file = Path("src/test/resources/turtle/example25.n3")
            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLFunction(
                                    "list",
                                    listOf(
                                        FOLConstant("\"1\"^^http://www.w3.org/2001/XMLSchema#integer"),
                                        FOLVariable("BN_1"),
                                        FOLFunction(
                                            "list",
                                            listOf(
                                                FOLConstant("\"2\"^^http://www.w3.org/2001/XMLSchema#integer")
                                            )
                                        )
                                    )
                                ),
                                FOLConstant("http://example.org/stuff/1.0/p2"),
                                FOLConstant("http://example.org/stuff/1.0/q2")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("BN_1"),
                                FOLConstant("http://example.org/stuff/1.0/p"),
                                FOLConstant("http://example.org/stuff/1.0/q")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }
        should("transform example26.n3 without exception") {
            val file = Path("src/test/resources/turtle/example26.n3")
            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("B0"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                FOLConstant("\"1\"^^http://www.w3.org/2001/XMLSchema#integer")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("B0"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                FOLVariable("B1")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("B1"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                FOLVariable("B2")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("B2"),
                                FOLConstant("http://example.org/stuff/1.0/p"),
                                FOLConstant("http://example.org/stuff/1.0/q")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("B1"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                FOLVariable("B3")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("B3"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                FOLVariable("B4")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("B4"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                FOLConstant("\"2\"^^http://www.w3.org/2001/XMLSchema#integer")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("B4"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("B3"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }


        context("blogic") {
            should("transform blogic abc.n3s") {
                val file = Path("src/test/resources/blogic/abc.n3s")
                val solutionFile = Path("src/test/resources/blogic/abc.p")

                val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                        useRDFLists = true
                    ).getSuccessOrNull().shouldNotBeNull().positiveSurface
                ).getSuccessOrNull().shouldNotBeNull()
                result.joinToString(System.lineSeparator())
                    .replace("\\s+".toRegex(), "") shouldBeEqualComparingTo solutionFile.readText()
                    .replace("\\s+".toRegex(), "")
            }
            should("transform blogic abcd.n3s") {
                val file = Path("src/test/resources/blogic/abcd.n3s")
                val solutionFile = Path("src/test/resources/blogic/abcd.p")

                RdfSurfaceModelToTPTPModelUseCase().invoke(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                        useRDFLists = true
                    ).getSuccessOrNull().shouldNotBeNull().positiveSurface
                ).getSuccessOrNull().shouldNotBeNull().joinToString(System.lineSeparator())
                    .replace("\\s+".toRegex(), "") shouldBeEqualComparingTo solutionFile.readText()
                    .replace("\\s+".toRegex(), "")
            }
        }
        context("lists") {
            should("transform lists.n3 without exception") {
                val file = Path("src/test/resources/turtle/lists.n3")
                val solutionFile = Path("src/test/resources/turtle/lists.p")
                val result = (RdfSurfaceModelToTPTPModelUseCase().invoke(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                        useRDFLists = true
                    ).getSuccessOrNull().shouldNotBeNull().positiveSurface
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
                val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                    RDFSurfaceParseServiceImpl(LiteralServiceImpl()).parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                        useRDFLists = true
                    ).getSuccessOrNull().shouldNotBeNull().positiveSurface
                ).getSuccessOrNull().shouldNotBeNull().joinToString(System.lineSeparator()).replace(
                    "\\s".toRegex(),
                    ""
                ) shouldBeEqualComparingTo solutionFile.readText().replace("\\s".toRegex(), "")
            }
        }
    }
)
