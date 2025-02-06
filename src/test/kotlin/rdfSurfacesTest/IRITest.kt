package rdfSurfacesTest

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import entities.rdfsurfaces.rdf_term.IRI
import entities.rdfsurfaces.rdf_term.IRI.Companion.transformReference

class IRITest : ShouldSpec(
    {
        val baseIRI = IRI.from("http://a/b/c/d;p?q")

        context("Normal examples") {
            should("transform to absolute URI") {
                transformReference(IRI.from("g:h"), baseIRI).iri shouldBe "g:h"
                transformReference(IRI.from("g"), baseIRI).iri shouldBe "http://a/b/c/g"
                transformReference(IRI.from("./g"), baseIRI).iri shouldBe "http://a/b/c/g"
                transformReference(IRI.from("g/"), baseIRI).iri shouldBe "http://a/b/c/g/"
                transformReference(IRI.from("/g"), baseIRI).iri shouldBe "http://a/g"
                transformReference(IRI.from("//g"), baseIRI).iri shouldBe "http://g"
                transformReference(IRI.from("?y"), baseIRI).iri shouldBe "http://a/b/c/d;p?y"
                transformReference(IRI.from("g?y"), baseIRI).iri shouldBe "http://a/b/c/g?y"
                transformReference(IRI.from("#s"), baseIRI).iri shouldBe "http://a/b/c/d;p?q#s"
                transformReference(IRI.from("g#s"), baseIRI).iri shouldBe "http://a/b/c/g#s"

                transformReference(IRI.from("g?y#s"), baseIRI).iri shouldBe "http://a/b/c/g?y#s"
                transformReference(IRI.from(";x"), baseIRI).iri shouldBe "http://a/b/c/;x"
                transformReference(IRI.from("g;x"), baseIRI).iri shouldBe "http://a/b/c/g;x"
                transformReference(IRI.from("g;x?y#s"), baseIRI).iri shouldBe "http://a/b/c/g;x?y#s"
                transformReference(IRI.from(""), baseIRI).iri shouldBe "http://a/b/c/d;p?q"

                transformReference(IRI.from("."), baseIRI).iri shouldBe "http://a/b/c/"
                transformReference(IRI.from("./"), baseIRI).iri shouldBe "http://a/b/c/"
                transformReference(IRI.from(".."), baseIRI).iri shouldBe "http://a/b/"
                transformReference(IRI.from("../"), baseIRI).iri shouldBe "http://a/b/"
                transformReference(IRI.from("../g"), baseIRI).iri shouldBe "http://a/b/g"

                transformReference(IRI.from("../.."), baseIRI).iri shouldBe "http://a/"
                transformReference(IRI.from("../../"), baseIRI).iri shouldBe "http://a/"
                transformReference(IRI.from("../../g"), baseIRI).iri shouldBe "http://a/g"
            }
        }

        context("Abnormal examples") {
            should("transform to absolute URI"){
                transformReference(IRI.from("../../../g"), baseIRI).iri shouldBe "http://a/g"
                transformReference(IRI.from("../../../../g"), baseIRI).iri shouldBe "http://a/g"

                transformReference(IRI.from("/./g"), baseIRI).iri shouldBe "http://a/g"
                transformReference(IRI.from("/../g"), baseIRI).iri shouldBe "http://a/g"
                transformReference(IRI.from("g."), baseIRI).iri shouldBe "http://a/b/c/g."
                transformReference(IRI.from(".g"), baseIRI).iri shouldBe "http://a/b/c/.g"
                transformReference(IRI.from("g.."), baseIRI).iri shouldBe "http://a/b/c/g.."
                transformReference(IRI.from("..g"), baseIRI).iri shouldBe "http://a/b/c/..g"

                transformReference(IRI.from( "./../g"   ), baseIRI).iri shouldBe "http://a/b/g"
                transformReference(IRI.from( "./g/."   ), baseIRI).iri shouldBe "http://a/b/c/g/"
                transformReference(IRI.from( "g/./h"  ), baseIRI).iri shouldBe  "http://a/b/c/g/h"
                transformReference(IRI.from( "g/../h" ), baseIRI).iri shouldBe "http://a/b/c/h"
                transformReference(IRI.from("g;x=1/./y" ), baseIRI).iri shouldBe "http://a/b/c/g;x=1/y"
                transformReference(IRI.from( "g;x=1/../y"), baseIRI).iri shouldBe "http://a/b/c/y"

                transformReference(IRI.from(  "g?y/./x"  ), baseIRI).iri shouldBe   "http://a/b/c/g?y/./x"
                transformReference(IRI.from(  "g?y/../x"  ), baseIRI).iri shouldBe "http://a/b/c/g?y/../x"
                transformReference(IRI.from("g#s/./x"    ), baseIRI).iri shouldBe "http://a/b/c/g#s/./x"
                transformReference(IRI.from(  "g#s/../x"), baseIRI).iri shouldBe "http://a/b/c/g#s/../x"
            }
        }
    }
)