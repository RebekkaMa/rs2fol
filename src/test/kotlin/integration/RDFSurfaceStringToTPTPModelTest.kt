package integration

import adapter.parser.RDFSurfaceParseServiceImpl
import app.use_cases.modelTransformer.RDFSurfaceModelToFOLModelUseCase
import app.use_cases.modelTransformer.RDFSurfaceModelToTPTPModelUseCase
import entities.fol.*
import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import entities.rdfsurfaces.NegativeSurface
import entities.rdfsurfaces.QuerySurface
import entities.rdfsurfaces.RdfTriple
import entities.rdfsurfaces.rdf_term.BlankNode
import entities.rdfsurfaces.rdf_term.IRI
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe
import util.commandResult.getSuccessOrNull
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.readText

class RdfSurfaceStringlToTPTPModelTest : ShouldSpec(
    {
        val rdfSurfaceParseService = RDFSurfaceParseServiceImpl()
        val rdfSurfaceModelToFOLModelUseCase = RDFSurfaceModelToFOLModelUseCase()

        should("transform example2.n3 without exception") {
            val file = Path("src/test/resources/turtle/example2.n3")
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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

            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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

            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = (RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLExists(
                    variables = listOf(
                        FOLVariable("a"),
                        FOLVariable("b"),
                        FOLVariable("c")
                    ),
                    FOLAnd(
                        listOf(
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("a"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                    FOLConstant("\"Alice\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("a"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/knows"),
                                    FOLVariable("b")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("b"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                    FOLConstant("\"Bob\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("b"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/knows"),
                                    FOLVariable("c")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("c"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/name"),
                                    FOLConstant("\"Eve\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("b"),
                                    FOLConstant("http://xmlns.com/foaf/0.1/mbox"),
                                    FOLConstant("file://" + file.absolute().parent.invariantSeparatorsPathString + "/bob@example.com")
                                )
                            )
                        )
                    )
                )
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example20.n3 without exception - list as functions") {
            val file = Path("src/test/resources/turtle/example20.n3")
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = false
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

        should("transform example20.n3 without exception - list as RDF collections") {
            val file = Path("src/test/resources/turtle/example20.n3")
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

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
                                    FOLConstant("http://example.org/stuff/1.0/a"),
                                    FOLConstant("http://example.org/stuff/1.0/b"),
                                    FOLVariable("BN_1")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_1"),
                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                    FOLConstant("\"apple\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_1"),
                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                    FOLVariable("BN_2")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_2"),
                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                    FOLConstant("\"banana\"^^http://www.w3.org/2001/XMLSchema#string")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("BN_2"),
                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                                )
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
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

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
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example22.n3 without exception") {
            val file = Path("src/test/resources/turtle/example22.n3")
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
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
                                FOLConstant("\"The first line\nThe second line\n  more\"^^http://www.w3.org/2001/XMLSchema#string")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("http://example.org/stuff/1.0/a"),
                                FOLConstant("http://example.org/stuff/1.0/b"),
                                FOLConstant("\"The first line\nThe second line\n  more\"^^http://www.w3.org/2001/XMLSchema#string")
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
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = false
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
                                FOLConstant("\"2.0\"^^http://www.w3.org/2001/XMLSchema#decimal"),
                                FOLConstant("\"3E1\"^^http://www.w3.org/2001/XMLSchema#double")
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
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLExists(
                    variables = listOf(
                        FOLVariable("b0"),
                        FOLVariable("b1"),
                        FOLVariable("b2")
                    ),
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
                                    FOLVariable("b1")
                                )
                            ),
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLVariable("b1"),
                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                    FOLConstant("\"2.0\"^^http://www.w3.org/2001/XMLSchema#decimal")
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
                                    FOLConstant("\"3E1\"^^http://www.w3.org/2001/XMLSchema#double")
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
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }

        should("transform example25.n3 without exception") {
            val file = Path("src/test/resources/turtle/example25.n3")
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = false
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLExists(
                    variables = listOf(
                        FOLVariable("BN_1")
                    ),
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
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }
        should("transform example26.n3 without exception") {
            val file = Path("src/test/resources/turtle/example26.n3")
            val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                rdfSurfaceParseService.parseToEnd(
                    file.readText(),
                    IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                    useRDFLists = true
                ).getSuccessOrNull().shouldNotBeNull().positiveSurface
            ).getSuccessOrNull()

            val expectedResult = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLExists(
                    variables = listOf(
                        FOLVariable("B0"),
                        FOLVariable("B1"),
                        FOLVariable("B2"),
                        FOLVariable("B3"),
                        FOLVariable("B4")
                    ),
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
            )

            result shouldNotBe null
            result!! shouldBeEqual listOf(expectedResult)
        }


        context("blogic") {
            should("transform blogic abc.n3s") {
                val file = Path("src/test/resources/blogic/abc.n3s")

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


                val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                        useRDFLists = true
                    ).getSuccessOrNull().shouldNotBeNull().positiveSurface
                ).getSuccessOrNull().shouldNotBeNull()

                val expectedResult1 = AnnotatedFormula(
                    "axiom",
                    FormulaType.Axiom,
                    FOLAnd(
                        listOf(
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLConstant("http://example.org/ns#i"),
                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                    FOLConstant("http://example.org/ns#A")
                                )
                            ),
                            FOLNot(
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLConstant("http://example.org/ns#i"),
                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                        FOLConstant("http://example.org/ns#C")
                                    )
                                )
                            ),
                            FOLForAll(
                                listOf(FOLVariable("S")),
                                FOLNot(
                                    FOLAnd(
                                        listOf(
                                            FOLPredicate(
                                                "triple",
                                                listOf(
                                                    FOLVariable("S"),
                                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                                    FOLConstant("http://example.org/ns#A")
                                                )
                                            ),
                                            FOLNot(
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("S"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                                        FOLConstant("http://example.org/ns#B")
                                                    )
                                                )
                                            ),
                                            FOLNot(
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("S"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                                        FOLConstant("http://example.org/ns#C")
                                                    )
                                                )
                                            )
                                        )
                                    )

                                )
                            )
                        )
                    )
                )
                val expectedResult2 = AnnotatedFormula(
                    "query_0",
                    FormulaType.Question,
                    FOLExists(
                        listOf(FOLVariable("S"), FOLVariable("C")),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("S"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                FOLVariable("C")
                            )
                        )
                    )
                )

                result.shouldNotBeNull()
                result.shouldHaveSize(2)
                result[0] shouldBeEqual expectedResult1
                result[1] shouldBeEqual expectedResult2
            }

            should("transform blogic abcd.n3s") {
                val file = Path("src/test/resources/blogic/abcd.n3s")

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

                val expectedResult1 = AnnotatedFormula(
                    "axiom",
                    FormulaType.Axiom,
                    FOLAnd(
                        listOf(
                            FOLPredicate(
                                "triple",
                                listOf(
                                    FOLConstant("http://example.org/ns#i"),
                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                    FOLConstant("http://example.org/ns#A")
                                )
                            ),
                            FOLNot(
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLConstant("http://example.org/ns#i"),
                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                        FOLConstant("http://example.org/ns#C")
                                    )
                                )
                            ),
                            FOLNot(
                                FOLNot(
                                    FOLNot(
                                        FOLPredicate(
                                            "triple",
                                            listOf(
                                                FOLConstant("http://example.org/ns#i"),
                                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                                FOLConstant("http://example.org/ns#D")
                                            )
                                        )
                                    )
                                )
                            ),
                            FOLForAll(
                                listOf(FOLVariable("S")),
                                FOLNot(
                                    FOLAnd(
                                        listOf(
                                            FOLPredicate(
                                                "triple",
                                                listOf(
                                                    FOLVariable("S"),
                                                    FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                                    FOLConstant("http://example.org/ns#A")
                                                )
                                            ),
                                            FOLNot(
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("S"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                                        FOLConstant("http://example.org/ns#B")
                                                    )
                                                )
                                            ),
                                            FOLNot(
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("S"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                                        FOLConstant("http://example.org/ns#C")
                                                    )
                                                )
                                            ),
                                            FOLNot(
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("S"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                                        FOLConstant("http://example.org/ns#D")
                                                    )
                                                )
                                            )
                                        )
                                    )

                                )
                            )
                        )
                    )
                )

                val expectedResult2 = AnnotatedFormula(
                    "query_0",
                    FormulaType.Question,
                    FOLExists(
                        listOf(FOLVariable("S"), FOLVariable("C")),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("S"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                FOLVariable("C")
                            )
                        )
                    )
                )

                val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                        useRDFLists = true
                    ).getSuccessOrNull().shouldNotBeNull().positiveSurface
                ).getSuccessOrNull().shouldNotBeNull()

                result.shouldNotBeNull()
                result.shouldHaveSize(2)
                result[0] shouldBeEqual expectedResult1
                result[1] shouldBeEqual expectedResult2

            }
        }


        context("lists") {

            should("transform lists.n3 without exception with RDF Collections") {

                val file = Path("src/test/resources/turtle/lists.n3")

                val expected = AnnotatedFormula(
                    name = "axiom",
                    type = FormulaType.Axiom,
                    expression = FOLExists(
                        variables = listOf(
                            FOLVariable("BN_1"),
                            FOLVariable("BN_2"),
                            FOLVariable("BN_3"),
                            FOLVariable("T"),
                            FOLVariable("M"),
                            FOLVariable("BN_4")
                        ),
                        expression = FOLAnd(
                            listOf(
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLVariable("T"),
                                        FOLConstant("http://example.org/foopredicate"),
                                        FOLVariable("BN_1")
                                    )
                                ),
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLVariable("BN_1"),
                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                        FOLConstant("http://example.org/fooA")
                                    )
                                ),
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLVariable("BN_1"),
                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                        FOLVariable("BN_2")
                                    )
                                ),
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLVariable("BN_2"),
                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                        FOLConstant("http://example.org/fooB")
                                    )
                                ),
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLVariable("BN_2"),
                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                        FOLVariable("BN_3")
                                    )
                                ),
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLVariable("BN_3"),
                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                        FOLConstant("http://example.org/fooC")
                                    )
                                ),
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLVariable("BN_3"),
                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                                    )
                                ),
                                FOLExists(
                                    variables = listOf(FOLVariable("T")),
                                    expression = FOLPredicate(
                                        "triple", listOf(
                                            FOLConstant("http://example.org/foosubject"),
                                            FOLConstant("http://example.org/foopredicate2"),
                                            FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                                        )
                                    )
                                ),
                                FOLForAll(
                                    variables = listOf(
                                        FOLVariable("T"),
                                        FOLVariable("O"),
                                        FOLVariable("BN_5"),
                                        FOLVariable("BN_6"),
                                        FOLVariable("BN_7"),
                                        FOLVariable("BN_8"),
                                        FOLVariable("BN_9"),
                                        FOLVariable("BN_10")
                                    ),
                                    expression = FOLNot(
                                        FOLAnd(
                                            listOf(
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLConstant("http://example.org/foosubject"),
                                                        FOLConstant("http://example.org/foopredicate2"),
                                                        FOLVariable("BN_8")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_4"),
                                                        FOLConstant("http://example.org/fooA"),
                                                        FOLConstant("http://example.org/fooC")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_5"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                                        FOLConstant("http://example.org/fooL")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_5"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                                        FOLVariable("BN_6")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_6"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                                        FOLVariable("M")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_6"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                                        FOLVariable("BN_7")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_7"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                                        FOLVariable("BN_4")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_7"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_8"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                                        FOLConstant("http://example.org/fooC")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_8"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                                        FOLVariable("BN_9")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_9"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                                        FOLConstant("http://example.org/fooF")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_9"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                                        FOLVariable("BN_10")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_10"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),
                                                        FOLVariable("BN_5")
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_10"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),
                                                        FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )


                val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                        useRDFLists = true
                    ).getSuccessOrNull().shouldNotBeNull().positiveSurface
                ).getSuccessOrNull().shouldNotBeNull()

                result.shouldNotBeNull()
                result.shouldHaveSize(1)
                result[0] shouldBeEqual expected
            }


            should("transform lists.n3 without exception (with RDF first-rest chains)") {
                val file = Path("src/test/resources/turtle/lists.n3")

                val expected = AnnotatedFormula(
                    name = "axiom",
                    type = FormulaType.Axiom,
                    expression = FOLExists(
                        variables = listOf(
                            FOLVariable("T"),
                            FOLVariable("M"),
                            FOLVariable("BN_1")
                        ),
                        expression = FOLAnd(
                            listOf(
                                FOLPredicate(
                                    "triple",
                                    listOf(
                                        FOLVariable("T"),
                                        FOLConstant("http://example.org/foopredicate"),
                                        FOLFunction(
                                            "list",

                                            listOf(
                                                FOLConstant("http://example.org/fooA"),
                                                FOLConstant("http://example.org/fooB"),
                                                FOLConstant("http://example.org/fooC")
                                            )
                                        )
                                    )
                                ),
                                FOLExists(
                                    variables = listOf(FOLVariable("T")),
                                    expression = FOLPredicate(
                                        "triple",
                                        listOf(
                                            FOLConstant("http://example.org/foosubject"),
                                            FOLConstant("http://example.org/foopredicate2"),
                                            FOLFunction(
                                                "list",
                                                emptyList()
                                            )
                                        )
                                    )
                                ),
                                FOLForAll(
                                    variables = listOf(
                                        FOLVariable("T"),
                                        FOLVariable("O")
                                    ),
                                    expression = FOLNot(
                                        FOLAnd(
                                            listOf(
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLConstant("http://example.org/foosubject"),
                                                        FOLConstant("http://example.org/foopredicate2"),
                                                        FOLFunction(
                                                            "list",
                                                            listOf(
                                                                FOLConstant("http://example.org/fooC"),
                                                                FOLConstant("http://example.org/fooF"),
                                                                FOLFunction(
                                                                    "list",
                                                                    listOf(
                                                                        FOLConstant("http://example.org/fooL"),
                                                                        FOLVariable("M"),
                                                                        FOLVariable("BN_1")
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    )
                                                ),
                                                FOLPredicate(
                                                    "triple",
                                                    listOf(
                                                        FOLVariable("BN_1"),
                                                        FOLConstant("http://example.org/fooA"),
                                                        FOLConstant("http://example.org/fooC")
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )


                val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(
                    rdfSurfaceParseService.parseToEnd(
                        file.readText(),
                        IRI.from("file://" + file.absolute().parent.invariantSeparatorsPathString + "/"),
                        useRDFLists = false
                    ).getSuccessOrNull().shouldNotBeNull().positiveSurface
                ).getSuccessOrNull().shouldNotBeNull()

                result.shouldNotBeNull()
                result.shouldHaveSize(1)
                result[0] shouldBeEqual expected
            }
        }
    }
)
