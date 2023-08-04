package rdfSurfacesTest

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import rdfSurfaces.IRI
import rdfSurfaces.IRI.Companion.transformReference

class IRITest : ShouldSpec(
    {
        val baseIRI = IRI.fromFullString("http://a/b/c/d;p?q")

        context("Normal examples") {
            should("transform to absolute URI") {
                transformReference(IRI.fromFullString("g:h"), baseIRI).iri shouldBe "g:h"
                transformReference(IRI.fromFullString("g"), baseIRI).iri shouldBe "http://a/b/c/g"
                transformReference(IRI.fromFullString("./g"), baseIRI).iri shouldBe "http://a/b/c/g"
                transformReference(IRI.fromFullString("g/"), baseIRI).iri shouldBe "http://a/b/c/g/"
                transformReference(IRI.fromFullString("/g"), baseIRI).iri shouldBe "http://a/g"
                transformReference(IRI.fromFullString("//g"), baseIRI).iri shouldBe "http://g"
                transformReference(IRI.fromFullString("?y"), baseIRI).iri shouldBe "http://a/b/c/d;p?y"
                transformReference(IRI.fromFullString("g?y"), baseIRI).iri shouldBe "http://a/b/c/g?y"
                transformReference(IRI.fromFullString("#s"), baseIRI).iri shouldBe "http://a/b/c/d;p?q#s"
                transformReference(IRI.fromFullString("g#s"), baseIRI).iri shouldBe "http://a/b/c/g#s"

                transformReference(IRI.fromFullString("g?y#s"), baseIRI).iri shouldBe "http://a/b/c/g?y#s"
                transformReference(IRI.fromFullString(";x"), baseIRI).iri shouldBe "http://a/b/c/;x"
                transformReference(IRI.fromFullString("g;x"), baseIRI).iri shouldBe "http://a/b/c/g;x"
                transformReference(IRI.fromFullString("g;x?y#s"), baseIRI).iri shouldBe "http://a/b/c/g;x?y#s"
                transformReference(IRI.fromFullString(""), baseIRI).iri shouldBe "http://a/b/c/d;p?q"

                transformReference(IRI.fromFullString("."), baseIRI).iri shouldBe "http://a/b/c/"
                transformReference(IRI.fromFullString("./"), baseIRI).iri shouldBe "http://a/b/c/"
                transformReference(IRI.fromFullString(".."), baseIRI).iri shouldBe "http://a/b/"
                transformReference(IRI.fromFullString("../"), baseIRI).iri shouldBe "http://a/b/"
                transformReference(IRI.fromFullString("../g"), baseIRI).iri shouldBe "http://a/b/g"

                transformReference(IRI.fromFullString("../.."), baseIRI).iri shouldBe "http://a/"
                transformReference(IRI.fromFullString("../../"), baseIRI).iri shouldBe "http://a/"
                transformReference(IRI.fromFullString("../../g"), baseIRI).iri shouldBe "http://a/g"
            }
        }

        context("Abnormal examples") {
            should("transform to absolute URI"){
                transformReference(IRI.fromFullString("../../../g"), baseIRI).iri shouldBe "http://a/g"
                transformReference(IRI.fromFullString("../../../../g"), baseIRI).iri shouldBe "http://a/g"

                transformReference(IRI.fromFullString("/./g"), baseIRI).iri shouldBe "http://a/g"
                transformReference(IRI.fromFullString("/../g"), baseIRI).iri shouldBe "http://a/g"
                transformReference(IRI.fromFullString("g."), baseIRI).iri shouldBe "http://a/b/c/g."
                transformReference(IRI.fromFullString(".g"), baseIRI).iri shouldBe "http://a/b/c/.g"
                transformReference(IRI.fromFullString("g.."), baseIRI).iri shouldBe "http://a/b/c/g.."
                transformReference(IRI.fromFullString("..g"), baseIRI).iri shouldBe "http://a/b/c/..g"

                transformReference(IRI.fromFullString( "./../g"   ), baseIRI).iri shouldBe "http://a/b/g"
                transformReference(IRI.fromFullString( "./g/."   ), baseIRI).iri shouldBe "http://a/b/c/g/"
                transformReference(IRI.fromFullString( "g/./h"  ), baseIRI).iri shouldBe  "http://a/b/c/g/h"
                transformReference(IRI.fromFullString( "g/../h" ), baseIRI).iri shouldBe "http://a/b/c/h"
                transformReference(IRI.fromFullString("g;x=1/./y" ), baseIRI).iri shouldBe "http://a/b/c/g;x=1/y"
                transformReference(IRI.fromFullString( "g;x=1/../y"), baseIRI).iri shouldBe "http://a/b/c/y"

                transformReference(IRI.fromFullString(  "g?y/./x"  ), baseIRI).iri shouldBe   "http://a/b/c/g?y/./x"
                transformReference(IRI.fromFullString(  "g?y/../x"  ), baseIRI).iri shouldBe "http://a/b/c/g?y/../x"
                transformReference(IRI.fromFullString("g#s/./x"    ), baseIRI).iri shouldBe "http://a/b/c/g#s/./x"
                transformReference(IRI.fromFullString(  "g#s/../x"), baseIRI).iri shouldBe "http://a/b/c/g#s/../x"
            }
        }
    }
)