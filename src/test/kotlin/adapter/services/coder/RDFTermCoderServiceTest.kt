package adapter.services.coder

import adapter.coder.N3SRDFTermCoderService
import entities.rdfsurfaces.rdf_term.BlankNode
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class RDFTermCoderServiceTest : ShouldSpec({
    val coderService = N3SRDFTermCoderService

    should("validate a correct blank node ID") {
        val blankNode = BlankNode("validBlankNodeID")
        println(N3SRDFTermCoderService.encode(blankNode))
        N3SRDFTermCoderService.isValid(blankNode) shouldBe true
    }

    should("invalidate an incorrect blank node ID") {
        val blankNode = BlankNode("invalidBlankNodeID!")
        N3SRDFTermCoderService.isValid(blankNode) shouldBe false
    }

    should("encode a blank node ID with special characters") {
        val blankNode = BlankNode("blankNodeID!")
        val encoded = N3SRDFTermCoderService.encode(blankNode)
        encoded.blankNodeId shouldBe "blankNodeIDOx0021"
    }

    should("encode a blank node ID without special characters") {
        val blankNode = BlankNode("blankNodeID")
        val encoded = N3SRDFTermCoderService.encode(blankNode)
        encoded.blankNodeId shouldBe "blankNodeID"
    }

    should("encode a blank node ID with existing U+ prefix") {
        val blankNode = BlankNode("blankNodeIDOx0021")
        val encoded = N3SRDFTermCoderService.encode(blankNode)
        encoded.blankNodeId shouldBe "blankNodeIDOx004FOx00780021"
    }
})