package useCaseTest

import entities.fol.*
import entities.rdfsurfaces.*
import entities.rdfsurfaces.rdf_term.BlankNode
import entities.rdfsurfaces.rdf_term.IRI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import util.commandResult.getSuccessOrNull
import kotlin.test.assertContains

class RdfSurfaceModelToTPTPModelUseCaseTestTest {

    @Test
    fun transformsBlankNodeToFOLVariable() {
        val blankNode = BlankNode("b1")
        val positiveSurfaceWithBlankNode =
            PositiveSurface(emptyList(), listOf(RdfTriple(blankNode, IRI(path = "p"), IRI(path = "o"))))

        val result = RdfSurfaceModelToTPTPModelUseCase(positiveSurfaceWithBlankNode)

        val expectedFormula = FOLPredicate("triple", listOf(FOLVariable("b1"), FOLConstant("p"), FOLConstant("o")))

        assertEquals(expectedFormula, result.getSuccessOrNull()?.single()?.expression)
    }

    @Test
    fun transformsIRIToFOLConstant() {
        val iri = IRI.from("http://example.org")
        val positiveSurfaceWithIRI =
            PositiveSurface(emptyList(), listOf(RdfTriple(iri, IRI(path = "p"), IRI(path = "o"))))
        val result = RdfSurfaceModelToTPTPModelUseCase(positiveSurfaceWithIRI)
        assertEquals(
            FOLPredicate(
                "triple",
                listOf(FOLConstant("http://example.org"), FOLConstant("p"), FOLConstant("o"))
            ), result.getSuccessOrNull()?.single()?.expression
        )
    }

    @Test
    fun transformsEmptyPositiveSurfaceToFOLTrue() {
        val positiveSurface = PositiveSurface(emptyList(), emptyList())
        val result = RdfSurfaceModelToTPTPModelUseCase(positiveSurface)
        assertTrue(result.isResult)
        assertEquals(FOLTrue, result.getSuccessOrNull()?.single()?.expression)
    }

    @Test
    fun transformsEmptyNegativeSurfaceToNegatedFOLTrue() {
        val negativeSurface = NegativeSurface(emptyList(), emptyList())
        val positiveSurface = PositiveSurface(emptyList(), listOf(negativeSurface))
        val result = RdfSurfaceModelToTPTPModelUseCase(positiveSurface)
        assertTrue(result.isResult)
        assertEquals(FOLNot(FOLTrue), result.getSuccessOrNull()?.single()?.expression)
    }

    @Test
    fun transformsPositiveSurfaceWithGraphToFOLAnd() {
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
        val result = RdfSurfaceModelToTPTPModelUseCase(positiveSurface)
        assertTrue(result.isResult)
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

    @Test
    fun throwsSurfaceNotSupportedExceptionForUnsupportedSurface() {
        val unsupportedSurface = NeutralSurface(emptyList(), emptyList())
        val positiveSurface = PositiveSurface(emptyList(), listOf(unsupportedSurface))

        val result = RdfSurfaceModelToTPTPModelUseCase(positiveSurface)
        assertTrue(result.isFailure)
    }

    @Test
    fun transformsQuerySurfaceToAnnotatedFormula() {
        val querySurface =
            QuerySurface(emptyList(), listOf(RdfTriple(IRI(path = "p"), IRI(path = "o"), IRI(path = "c"))))
        val positiveSurface = PositiveSurface(emptyList(), listOf(querySurface))
        val result = RdfSurfaceModelToTPTPModelUseCase(positiveSurface)
        assertTrue(result.isResult)
        val expectedExpression = FOLPredicate("triple", listOf(FOLConstant("p"), FOLConstant("o"), FOLConstant("c")))
        assertContains(result.getSuccessOrNull()?.map { it.expression } ?: listOf(), expectedExpression)
    }
}