package unit.entities.rdfsurfaces

import entities.rdfsurfaces.rdf_term.BlankNode
import entities.rdfsurfaces.rdf_term.DefaultLiteral
import entities.rdfsurfaces.rdf_term.IRI
import entities.rdfsurfaces.rdf_term.LanguageTaggedString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import util.IRIConstants
import util.commandResult.getSuccessOrNull

class QuerySurfaceTest : ShouldSpec({

    context("QuerySurface::replaceBlankNodes") {

        should("replace all Blank Nodes with the associated values") {

            val bn1 = BlankNode("Bn1")
            val bn2 = BlankNode("Bn2")
            val bn3 = BlankNode("Bn3")

            val bn1_r = BlankNode("Bn1_r")
            val bn2_r = IRI.from("http://example.com#bn2_r")
            val bn3_r = LanguageTaggedString("bn3_r", "en")

            val bn4 = BlankNode("Bn4")

            val iri1 = IRI.from("http://example.com#1")
            val iri2 = IRI.from("http://example.com#2")
            val iri3 = IRI.from("http://example.com#3")

            val literal1 = DefaultLiteral("4545", IRI.from(IRIConstants.XSD_INTEGER))
            val literal2 = LanguageTaggedString("cat", "en")
            val literal3 = DefaultLiteral(
                "Tomatensalat",
                IRI.from("http://www.w3.org/2001/XMLSchema#string"),
            )

            val triple11 = entities.rdfsurfaces.RdfTriple(bn1, iri1, literal1)
            val triple12 = entities.rdfsurfaces.RdfTriple(literal3, bn2, bn4)
            val triple13 = entities.rdfsurfaces.RdfTriple(iri3, literal2, bn3)

            val triple11_r = entities.rdfsurfaces.RdfTriple(bn1_r, iri1, literal1)
            val triple12_r = entities.rdfsurfaces.RdfTriple(literal3, bn2_r, bn4)
            val triple13_r = entities.rdfsurfaces.RdfTriple(iri3, literal2, bn3_r)

            val triple21 = entities.rdfsurfaces.RdfTriple(iri2, iri1, bn1)
            val triple22 = entities.rdfsurfaces.RdfTriple(iri3, bn2, iri2)
            val triple23 = entities.rdfsurfaces.RdfTriple(iri3, iri2, iri1)

            val triple21_r = entities.rdfsurfaces.RdfTriple(iri2, iri1, bn1_r)
            val triple22_r = entities.rdfsurfaces.RdfTriple(iri3, bn2_r, iri2)
            val triple23_r = entities.rdfsurfaces.RdfTriple(iri3, iri2, iri1)

            val positiveSurface21 = entities.rdfsurfaces.PositiveSurface(listOf(), listOf(triple21, triple22, triple23))
            val negativeSurface21 = entities.rdfsurfaces.NegativeSurface(listOf(), listOf(triple21, triple22, triple23))

            val positiveSurface22 =
                entities.rdfsurfaces.PositiveSurface(listOf(bn1, bn2, bn3), listOf(triple21, triple22, triple23))
            val negativeSurface22 =
                entities.rdfsurfaces.NegativeSurface(listOf(bn1, bn2, bn3), listOf(triple21, triple22, triple23))

            val positiveSurface21_r =
                entities.rdfsurfaces.PositiveSurface(listOf(), listOf(triple21_r, triple22_r, triple23_r))
            val negativeSurface21_r =
                entities.rdfsurfaces.NegativeSurface(listOf(), listOf(triple21_r, triple22_r, triple23_r))

            val positiveSurface11 = entities.rdfsurfaces.PositiveSurface(
                listOf(),
                listOf(
                    triple21,
                    triple22,
                    triple23,
                    positiveSurface21,
                    negativeSurface21,
                    positiveSurface22,
                    negativeSurface22
                )
            )
            val negativeSurface11 = entities.rdfsurfaces.NegativeSurface(
                listOf(),
                listOf(
                    triple21,
                    triple22,
                    triple23,
                    positiveSurface21,
                    negativeSurface21,
                    positiveSurface22,
                    negativeSurface22
                )
            )

            val positiveSurface12 = entities.rdfsurfaces.PositiveSurface(
                listOf(bn1, bn2, bn3),
                listOf(
                    triple21,
                    triple22,
                    triple23,
                    positiveSurface21,
                    negativeSurface21,
                    positiveSurface22,
                    negativeSurface22
                )
            )
            val negativeSurface12 = entities.rdfsurfaces.NegativeSurface(
                listOf(bn1, bn2, bn3),
                listOf(
                    triple21,
                    triple22,
                    triple23,
                    positiveSurface21,
                    negativeSurface21,
                    positiveSurface22,
                    negativeSurface22
                )
            )

            val positiveSurface11_r = entities.rdfsurfaces.PositiveSurface(
                listOf(),
                listOf(
                    triple21_r,
                    triple22_r,
                    triple23_r,
                    positiveSurface21_r,
                    negativeSurface21_r,
                    positiveSurface22,
                    negativeSurface22
                )
            )
            val negativeSurface11_r = entities.rdfsurfaces.NegativeSurface(
                listOf(),
                listOf(
                    triple21_r,
                    triple22_r,
                    triple23_r,
                    positiveSurface21_r,
                    negativeSurface21_r,
                    positiveSurface22,
                    negativeSurface22
                )
            )

            val positiveSurface12_r = entities.rdfsurfaces.PositiveSurface(
                listOf(bn1, bn2, bn3),
                listOf(
                    triple21,
                    triple22,
                    triple23,
                    positiveSurface21,
                    negativeSurface21,
                    positiveSurface22,
                    negativeSurface22
                )
            )
            val negativeSurface12_r = entities.rdfsurfaces.NegativeSurface(
                listOf(bn1, bn2, bn3),
                listOf(
                    triple21,
                    triple22,
                    triple23,
                    positiveSurface21,
                    negativeSurface21,
                    positiveSurface22,
                    negativeSurface22
                )
            )

            val querySurface = entities.rdfsurfaces.QuerySurface(
                listOf(bn1, bn2, bn3),
                listOf(
                    triple11,
                    positiveSurface11,
                    triple12,
                    negativeSurface11,
                    triple13,
                    positiveSurface12,
                    negativeSurface12
                )
            )
            val querySurface_r = entities.rdfsurfaces.PositiveSurface(
                listOf(),
                listOf(
                    triple11_r,
                    positiveSurface11_r,
                    triple12_r,
                    negativeSurface11_r,
                    triple13_r,
                    positiveSurface12_r,
                    negativeSurface12_r
                )
            )

            val result = querySurface.replaceBlankNodes(
                listOf(
                    listOf(
                        bn1_r,
                        bn2_r,
                        bn3_r
                    )
                )
            ).getSuccessOrNull().shouldNotBeNull()
            result shouldBe querySurface_r
        }
    }
})