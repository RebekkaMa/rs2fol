package controllerTest

import app.use_cases.modelTransformer.ListType
import app.use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import entities.fol.*
import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import entities.rdfsurfaces.NegativeSurface
import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.QuerySurface
import entities.rdfsurfaces.RdfTriple
import entities.rdfsurfaces.rdf_term.*
import entities.rdfsurfaces.rdf_term.Collection
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.graph.langtag.LangTags
import util.commandResult.getSuccessOrNull
import java.io.File

class RDFSurfaceModelToTPTPModelUseCaseTest
    : ShouldSpec(
    {
        context("turtle") {
            should("transform example2.n3 without exception") {

                val iri1 = IRI.from("http://example.org/#spiderman")
                val iri2 = IRI.from("http://www.perceive.net/schemas/relationship/enemyOf")
                val iri3 = IRI.from("http://example.org/#green-goblin")

                val rdfTriple = RdfTriple(iri1, iri2, iri3)

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

                val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                    PositiveSurface(
                        listOf(),
                        listOf(rdfTriple)
                    )
                ).getSuccessOrNull()

                result.shouldNotBeNull()
                result.shouldBeSingleton()
                result.single() shouldBeEqual expectedResult
            }

            should("transform example11.n3 without exception") {

                val iri1 = IRI.from("http://example.org/vocab/show/218")
                val iri2 = IRI.from("http://www.w3.org/2000/01/rdf-schema#label")
                val iri3 = IRI.from("http://example.org/vocab/show/localName")
                val iri4 = IRI.from("http://example.org/vocab/show/blurb")

                val literal1 =
                    DefaultLiteral(
                        lexicalValue = "That Seventies Show",
                        datatypeIRI = IRI.from(XSDDatatype.XSDstring.uri),
                        literalValue = "That Seventies Show"
                    )
                val literal2 =
                    LanguageTaggedString(
                        "That Seventies Show",
                        "en",
                        LangTags.formatLangtag("en")
                    )
                val literal3 =
                    LanguageTaggedString("Cette Série des Années Soixante-dix", "fr", LangTags.formatLangtag("fr"))
                val literal4 =
                    LanguageTaggedString("Cette Série des Années Septante", "fr-be", LangTags.formatLangtag("fr-be"))
                val literal5Value =
                    "This is a multi-line                        # literal with embedded new lines and quotes\n" +
                            "literal with many quotes (\"\"\"\"\")\n" +
                            "and up to two sequential apostrophes ('')."
                val literal5 = DefaultLiteral(literal5Value, IRI.from(XSDDatatype.XSDstring.uri), literal5Value)

                val rdfTriple1 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple2 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple3 = RdfTriple(iri1, iri2, literal1)
                val rdfTriple4 = RdfTriple(iri1, iri3, literal2)
                val rdfTriple5 = RdfTriple(iri1, iri3, literal3)
                val rdfTriple6 = RdfTriple(iri1, iri3, literal4)
                val rdfTriple7 = RdfTriple(iri1, iri4, literal5)

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
                    )


                val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                    PositiveSurface(
                        listOf(),
                        listOf(rdfTriple1, rdfTriple2, rdfTriple3, rdfTriple4, rdfTriple5, rdfTriple6, rdfTriple7)
                    )
                ).getSuccessOrNull()
                result.shouldNotBeNull()
                result.shouldBeSingleton()
                result.single() shouldBeEqual expectedResult
            }

            should("transform example23.n3 without exception - non d entailment") {
                val solutionFile = File("src/test/resources/turtle/example23.p")

                val iri1 = IRI.from("http://example.org/stuff/1.0/p")

                val literal1 = DefaultLiteral("w", IRI.from(XSDDatatype.XSDstring.uri), "w")
                val literal2 = DefaultLiteral("1", IRI.from(XSDDatatype.XSDinteger.uri), 1)
                val literal3 = DefaultLiteral(
                    XSDDatatype.XSDdecimal.parse("2.0").toString(),
                    IRI.from(XSDDatatype.XSDdecimal.uri),
                    2.0
                )
                val literal4 = DefaultLiteral("3E1", IRI.from(XSDDatatype.XSDdouble.uri), 30.0)


                val collection1 =
                    Collection(listOf(literal2, literal3, literal4))

                val rdfTriple1 = RdfTriple(collection1, iri1, literal1)

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
                                    FOLConstant("\"3E1\"^^http://www.w3.org/2001/XMLSchema#double")
                                )
                            ),
                            FOLConstant("http://example.org/stuff/1.0/p"),
                            FOLConstant("\"w\"^^http://www.w3.org/2001/XMLSchema#string")
                        )
                    )
                )


                val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                    PositiveSurface(
                        listOf(), listOf(rdfTriple1)
                    ),
                    dEntailment = false
                ).getSuccessOrNull()
                result.shouldNotBeNull()
                result.shouldBeSingleton()
                result.single() shouldBeEqual expectedResult
            }

            should("transform example23.n3 without exception - d entailment") {
                val solutionFile = File("src/test/resources/turtle/example23.p")

                val iri1 = IRI.from("http://example.org/stuff/1.0/p")

                val literal1 = DefaultLiteral("w", IRI.from(XSDDatatype.XSDstring.uri), "w")
                val literal2 = DefaultLiteral("1", IRI.from(XSDDatatype.XSDinteger.uri), 1)
                val literal3 = DefaultLiteral(
                    XSDDatatype.XSDdecimal.parse("2.0").toString(),
                    IRI.from(XSDDatatype.XSDdecimal.uri),
                    2.0
                )
                val literal4 = DefaultLiteral("3E1", IRI.from(XSDDatatype.XSDdouble.uri), 30.0)


                val collection1 =
                    Collection(listOf(literal2, literal3, literal4))

                val rdfTriple1 = RdfTriple(collection1, iri1, literal1)

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
                                    FOLConstant("\"30.0\"^^http://www.w3.org/2001/XMLSchema#double")
                                )
                            ),
                            FOLConstant("http://example.org/stuff/1.0/p"),
                            FOLConstant("\"w\"^^http://www.w3.org/2001/XMLSchema#string")
                        )
                    )
                )

                val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                    PositiveSurface(
                        listOf(), listOf(rdfTriple1)
                    ),
                    dEntailment = true
                ).getSuccessOrNull()
                result.shouldNotBeNull()
                result.shouldBeSingleton()
                result.single() shouldBeEqual expectedResult
            }
        }

        context("blogic") {
            should("transform blogic abc.n3s") {

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


                val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                    PositiveSurface(
                        listOf(),
                        listOf(rdfTriple1, negativeSurface1, negativeSurface2, querySurface)
                    ),
                    ignoreQuerySurfaces = false,
                ).getSuccessOrNull()

                result.shouldNotBeNull()
                result.shouldHaveSize(2)
                result[0] shouldBeEqual expectedResult1
                result[1] shouldBeEqual expectedResult2
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

                val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                    PositiveSurface(
                        listOf(),
                        listOf(rdfTriple1, negativeSurface1, negativeSurface2, negativeSurface3, querySurface)
                    )
                ).getSuccessOrNull()

                result.shouldNotBeNull()
                result.shouldHaveSize(2)
                result[0] shouldBeEqual expectedResult1
                result[1] shouldBeEqual expectedResult2
            }

            should("ignore query surfaces") {

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

                val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                    PositiveSurface(
                        listOf(),
                        listOf(rdfTriple1, negativeSurface1, negativeSurface2, querySurface)
                    ),
                    ignoreQuerySurfaces = true,
                ).getSuccessOrNull()

                result.shouldNotBeNull()
                result.shouldBeSingleton()
                result.single() shouldBeEqual expectedResult1
            }
        }

        should("map lists to functions") {
            val iri1 = IRI.from("http://example.org/ns#i")
            val iri2 = IRI.from("http://example.org/ns#A")
            val iri5 = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")

            val bnS = BlankNode("S")
            val bnC = BlankNode("C")

            val literal1 = DefaultLiteral("1", IRI.from(XSDDatatype.XSDinteger.uri), 1)
            val literal2 = DefaultLiteral("2", IRI.from(XSDDatatype.XSDinteger.uri), 2)
            val literal3 = DefaultLiteral("3", IRI.from(XSDDatatype.XSDinteger.uri), 3)

            val literal4 = DefaultLiteral("4", IRI.from(XSDDatatype.XSDinteger.uri), 4)
            val literal5 = DefaultLiteral("5", IRI.from(XSDDatatype.XSDinteger.uri), 5)

            val collection1 = Collection(listOf(literal1, literal2, literal3))
            val collection2 = Collection(listOf(collection1, literal4, literal5))

            val rdfTriple1 = RdfTriple(collection1, iri1, iri2)
            val rdfTriple2 = RdfTriple(bnS, iri5, collection2)
            val rdfTriple3 = RdfTriple(bnS, iri5, bnC)

            val fofList1 = FOLFunction(
                "list",
                listOf(
                    FOLConstant("\"1\"^^http://www.w3.org/2001/XMLSchema#integer"),
                    FOLConstant("\"2\"^^http://www.w3.org/2001/XMLSchema#integer"),
                    FOLConstant("\"3\"^^http://www.w3.org/2001/XMLSchema#integer")
                )
            )

            val fofList2 = FOLFunction(
                "list",
                listOf(
                    fofList1,
                    FOLConstant("\"4\"^^http://www.w3.org/2001/XMLSchema#integer"),
                    FOLConstant("\"5\"^^http://www.w3.org/2001/XMLSchema#integer")
                )
            )

            val expectedResult1 = AnnotatedFormula(
                "axiom",
                FormulaType.Axiom,
                FOLAnd(
                    listOf(
                        FOLPredicate(
                            "triple",
                            listOf(
                                fofList1,
                                FOLConstant("http://example.org/ns#i"),
                                FOLConstant("http://example.org/ns#A")
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("S"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                fofList2
                            )
                        ),
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLVariable("S"),
                                FOLConstant("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                                FOLVariable("C")
                            )
                        )
                    ),
                )
            )

            val result = RdfSurfaceModelToTPTPModelUseCase().invoke(
                PositiveSurface(
                    listOf(),
                    listOf(rdfTriple1, rdfTriple2, rdfTriple3)
                ),
                listType = ListType.FUNCTION
            ).getSuccessOrNull()

            result.shouldNotBeNull()
            result.shouldBeSingleton()
            result.single() shouldBeEqual expectedResult1
        }


    })