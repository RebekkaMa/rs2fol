package unit.app.use_cases.modelTransformer

import app.use_cases.modelTransformer.RDFSurfaceModelToFOLModelUseCase
import app.use_cases.modelTransformer.RDFSurfaceModelToTPTPModelUseCase
import app.use_cases.results.modelTransformerResults.RDFSurfaceModelToFOLModelResult
import entities.fol.*
import entities.rdfsurfaces.*
import entities.rdfsurfaces.rdf_term.BlankNode
import entities.rdfsurfaces.rdf_term.IRI
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.types.shouldBeTypeOf
import util.commandResult.getErrorOrNull
import util.commandResult.getSuccessOrNull
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RdfSurfaceModelToTPTPModelUseCaseTest : ShouldSpec({

    val rdfSurfaceModelToFOLModelUseCase = RDFSurfaceModelToFOLModelUseCase()

    should("transform BlankNode to FOLVariable") {
        val blankNode = BlankNode("b1")
        val positiveSurfaceWithBlankNode =
            PositiveSurface(emptyList(), listOf(RdfTriple(blankNode, IRI(path = "p"), IRI(path = "o"))))

        val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(positiveSurfaceWithBlankNode)

        val expectedFormula = FOLPredicate("triple", listOf(FOLVariable("b1"), FOLConstant("p"), FOLConstant("o")))

        assertEquals(expectedFormula, result.getSuccessOrNull()?.single()?.expression)
    }

    should("transform IRI to FOLConstant") {
        val iri = IRI.from("http://example.org")
        val positiveSurfaceWithIRI =
            PositiveSurface(emptyList(), listOf(RdfTriple(iri, IRI(path = "p"), IRI(path = "o"))))
        val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(positiveSurfaceWithIRI)
        assertEquals(
            FOLPredicate(
                "triple",
                listOf(FOLConstant("http://example.org"), FOLConstant("p"), FOLConstant("o"))
            ), result.getSuccessOrNull()?.single()?.expression
        )
    }

    should("transform empty PositiveSurface to FOLTrue") {
        val positiveSurface = PositiveSurface(emptyList(), emptyList())
        val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(positiveSurface)
        assertTrue(result.isSuccess)
        assertEquals(FOLTrue, result.getSuccessOrNull()?.single()?.expression)
    }

    should("transform empty NegativeSurface to negated FOLTrue") {
        val negativeSurface = NegativeSurface(emptyList(), emptyList())
        val positiveSurface = PositiveSurface(emptyList(), listOf(negativeSurface))
        val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(positiveSurface)
        assertTrue(result.isSuccess)
        assertEquals(FOLNot(FOLTrue), result.getSuccessOrNull()?.single()?.expression)
    }

    should("transform PositiveSurface with graph to FOLAnd") {
        val negativeSurface =
            NegativeSurface(
                listOf(BlankNode("bn1")),
                listOf(RdfTriple(IRI(path = "l"), BlankNode("bn1"), IRI(path = "n")))
            )
        val positiveSurface = PositiveSurface(
            emptyList(),
            listOf(
                RdfTriple(IRI(path = "p"), IRI(path = "o"), IRI(path = "c")),
                RdfTriple(IRI(path = "s"), IRI(path = "p"), IRI(path = "o")),
                negativeSurface
            )
        )
        val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(positiveSurface)
        assertTrue(result.isSuccess)
        val expectedExpression = FOLAnd(
            listOf(
                FOLPredicate(
                    "triple",
                    listOf(
                        FOLConstant("p"),
                        FOLConstant("o"),
                        FOLConstant("c")
                    )
                ),
                FOLPredicate(
                    "triple",
                    listOf(
                        FOLConstant("s"),
                        FOLConstant("p"),
                        FOLConstant("o")
                    )
                ),
                FOLForAll(
                    listOf(FOLVariable("bn1")),
                    FOLNot(
                        FOLPredicate(
                            "triple",
                            listOf(
                                FOLConstant("l"),
                                FOLVariable("bn1"),
                                FOLConstant("n")
                            )
                        )
                    )
                )
            )
        )
        assertEquals(expectedExpression, result.getSuccessOrNull()?.single()?.expression)
    }

    should("return SurfaceNotSupported Error for unsupported surface") {
        val unsupportedSurface = NeutralSurface(emptyList(), emptyList())
        val positiveSurface = PositiveSurface(emptyList(), listOf(unsupportedSurface))

        val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(positiveSurface).getErrorOrNull()
        result.shouldBeTypeOf<RDFSurfaceModelToFOLModelResult.Error.SurfaceNotSupported>()
    }

    should("transform QuerySurface to AnnotatedFormula") {
        val querySurface =
            QuerySurface(emptyList(), listOf(RdfTriple(IRI(path = "p"), IRI(path = "o"), IRI(path = "c"))))
        val positiveSurface = PositiveSurface(emptyList(), listOf(querySurface))
        val result = RDFSurfaceModelToTPTPModelUseCase(rdfSurfaceModelToFOLModelUseCase).invoke(positiveSurface)
        assertTrue(result.isSuccess)
        val expectedExpression = FOLPredicate("triple", listOf(FOLConstant("p"), FOLConstant("o"), FOLConstant("c")))
        assertContains(result.getSuccessOrNull()?.map { it.expression } ?: listOf(), expectedExpression)
    }
})