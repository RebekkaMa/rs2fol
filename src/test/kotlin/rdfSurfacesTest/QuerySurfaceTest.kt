package rdfSurfacesTest

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import rdfSurfaces.*

class QuerySurfaceTest : ShouldSpec({

    context("QuerySurface::replaceBlankNodes"){

        should("replace all Blank Nodes with the associated values"){

            val bn1 = BlankNode("Bn1")
            val bn2 = BlankNode("Bn2")
            val bn3 = BlankNode("Bn3")

            val bn1_r = BlankNode("Bn1_r")
            val bn2_r = IRI.from("http://example.com#bn2_r")
            val bn3_r = Literal.fromNonNumericLiteral("bn3_r", "en")

            val bn4 = BlankNode("Bn4")

            val iri1 = IRI.from("http://example.com#1")
            val iri2 = IRI.from("http://example.com#2")
            val iri3 = IRI.from("http://example.com#3")

            val literal1 = Literal.fromNumericLiteral("4545")
            val literal2 = Literal.fromNonNumericLiteral("cat", "en")
            val literal3 = Literal.fromNonNumericLiteral("Tomatensalat", IRI.from("http://www.w3.org/2001/XMLSchema#string"))

            val triple11 = RdfTriple(bn1, iri1, literal1)
            val triple12 = RdfTriple(literal3, bn2, bn4)
            val triple13 = RdfTriple(iri3, literal2, bn3)

            val triple11_r = RdfTriple(bn1_r, iri1, literal1)
            val triple12_r = RdfTriple(literal3, bn2_r, bn4)
            val triple13_r = RdfTriple(iri3, literal2, bn3_r)

            val triple21 = RdfTriple(iri2, iri1, bn1)
            val triple22 = RdfTriple(iri3, bn2, iri2)
            val triple23 = RdfTriple(iri3, iri2, iri1)

            val triple21_r = RdfTriple(iri2, iri1, bn1_r)
            val triple22_r = RdfTriple(iri3, bn2_r, iri2)
            val triple23_r = RdfTriple(iri3, iri2, iri1)

            val positiveSurface21 = PositiveSurface(listOf(), listOf(triple21, triple22, triple23))
            val negativeSurface21 = NegativeSurface(listOf(), listOf(triple21, triple22, triple23))

            val positiveSurface22 = PositiveSurface(listOf(bn1,bn2,bn3), listOf(triple21, triple22, triple23))
            val negativeSurface22 = NegativeSurface(listOf(bn1,bn2,bn3), listOf(triple21, triple22, triple23))

            val positiveSurface21_r = PositiveSurface(listOf(), listOf(triple21_r, triple22_r, triple23_r))
            val negativeSurface21_r = NegativeSurface(listOf(), listOf(triple21_r, triple22_r, triple23_r))

            val positiveSurface11 = PositiveSurface(listOf(), listOf(triple21, triple22, triple23, positiveSurface21, negativeSurface21, positiveSurface22, negativeSurface22))
            val negativeSurface11 = NegativeSurface(listOf(), listOf(triple21, triple22, triple23, positiveSurface21, negativeSurface21, positiveSurface22, negativeSurface22))

            val positiveSurface12 = PositiveSurface(listOf(bn1,bn2,bn3), listOf(triple21, triple22, triple23, positiveSurface21, negativeSurface21, positiveSurface22, negativeSurface22))
            val negativeSurface12 = NegativeSurface(listOf(bn1,bn2,bn3), listOf(triple21, triple22, triple23, positiveSurface21, negativeSurface21, positiveSurface22, negativeSurface22))

            val positiveSurface11_r = PositiveSurface(listOf(), listOf(triple21_r, triple22_r, triple23_r, positiveSurface21_r, negativeSurface21_r, positiveSurface22, negativeSurface22))
            val negativeSurface11_r = NegativeSurface(listOf(), listOf(triple21_r, triple22_r, triple23_r, positiveSurface21_r, negativeSurface21_r, positiveSurface22, negativeSurface22 ))

            val positiveSurface12_r = PositiveSurface(listOf(bn1,bn2,bn3), listOf(triple21, triple22, triple23, positiveSurface21, negativeSurface21, positiveSurface22, negativeSurface22))
            val negativeSurface12_r = NegativeSurface(listOf(bn1,bn2,bn3), listOf(triple21, triple22, triple23, positiveSurface21, negativeSurface21, positiveSurface22, negativeSurface22))

            val querySurface = QuerySurface(listOf(bn1,bn2,bn3), listOf(triple11, positiveSurface11, triple12, negativeSurface11, triple13, positiveSurface12, negativeSurface12))
            val querySurface_r = PositiveSurface(listOf(), listOf(triple11_r, positiveSurface11_r, triple12_r, negativeSurface11_r, triple13_r, positiveSurface12_r, negativeSurface12_r))

            querySurface.replaceBlankNodes(setOf(listOf(bn1_r, bn2_r, bn3_r))) shouldBeEqualToComparingFields querySurface_r
        }
    }
})