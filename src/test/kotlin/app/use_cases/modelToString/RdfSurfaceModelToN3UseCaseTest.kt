package app.use_cases.modelToString

import adapter.coder.N3SRDFTermCoderServiceImpl
import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.RdfTriple
import entities.rdfsurfaces.rdf_term.BlankNode
import entities.rdfsurfaces.rdf_term.IRI
import io.kotest.core.spec.style.ShouldSpec
import util.commandResult.getSuccessOrNull
import kotlin.test.assertEquals

class RdfSurfaceModelToN3UseCaseTest : ShouldSpec({

    should("convert a simple positive surface to N3S string") {
        val positiveSurface = PositiveSurface(
            graffiti = listOf(BlankNode("b1")),
            hayesGraph = listOf(
                RdfTriple(
                    rdfSubject = BlankNode("b1"),
                    rdfPredicate = IRI.from("http://example.org/predicate"),
                    rdfObject = IRI.from("http://example.org/object")
                )
            )
        )
        val expected = """
(_:b1) log:onPositiveSurface {
   _:b1 <http://example.org/predicate> <http://example.org/object>.
}.
""".trimIndent()
        val result = RdfSurfaceModelToN3UseCase(N3SRDFTermCoderServiceImpl()).invoke(
            positiveSurface,
            false,
        ).getSuccessOrNull()
        assertEquals(expected, result)
    }

    should("add well known prefixes to N3S") {
        val positiveSurface = PositiveSurface(
            graffiti = listOf(BlankNode("b1")),
            hayesGraph = listOf(
                RdfTriple(
                    rdfSubject = BlankNode("b1"),
                    rdfPredicate = IRI.from("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    rdfObject = IRI.from("http://www.w3.org/2000/10/swap/log#list")
                ),
                RdfTriple(
                    rdfSubject = BlankNode("b1"),
                    rdfPredicate = IRI.from("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
                    rdfObject = IRI.from("http://www.w3.org/2001/XMLSchema#string")
                ),
                RdfTriple(
                    rdfSubject = BlankNode("b1"),
                    rdfPredicate = IRI.from("http://example.org/predicate#test"),
                    rdfObject = IRI.from("http://example.org/object#test")
                ),
                RdfTriple(
                    rdfSubject = BlankNode("b1"),
                    rdfPredicate = IRI.from("http://example.org/abc#test"),
                    rdfObject = IRI.from("http://example.org/def#test")
                )

            )
        )
        val expected = """
@prefix ex4: <http://example.org/def#>.
@prefix ex3: <http://example.org/abc#>.
@prefix ex: <http://example.org/object#>.
@prefix : <http://example.org/predicate#>.
@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.
@prefix log: <http://www.w3.org/2000/10/swap/log#>.
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.
(_:b1) log:onPositiveSurface {
   _:b1 rdf:type log:list.
   _:b1 rdfs:subClassOf xsd:string.
   _:b1 :test ex:test.
   _:b1 ex3:test ex4:test.
}.
""".trimIndent()
        val result = RdfSurfaceModelToN3UseCase(N3SRDFTermCoderServiceImpl()).invoke(
            positiveSurface,
            encode = false
        ).getSuccessOrNull()
        assertEquals(expected, result)
    }

    should("handle empty positive surface") {
        val positiveSurface = PositiveSurface()
        val expected = ""
        val result = RdfSurfaceModelToN3UseCase(N3SRDFTermCoderServiceImpl()).invoke(
            positiveSurface,
            encode = false
        ).getSuccessOrNull()
        assertEquals(expected, result?.trim())
    }
})
