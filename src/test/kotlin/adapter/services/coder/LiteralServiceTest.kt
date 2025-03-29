package adapter.services.coder

import adapter.jena.XSDLiteralServiceImpl
import entities.rdfsurfaces.rdf_term.DefaultLiteral
import entities.rdfsurfaces.rdf_term.IRI.Companion.from
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.apache.jena.datatypes.xsd.XSDDatatype
import util.IRIConstants
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LiteralServiceTest : ShouldSpec({

    val literalService = XSDLiteralServiceImpl()
    should("create DefaultLiteral with date time literals") {
        val literal1 = literalService.createDefaultLiteral("2002-05-30T09:30:10Z", from(IRIConstants.XSD_DATE_TIME))
        val literal2 =
            literalService.createDefaultLiteral("2002-05-30T03:30:10-06:00", from(IRIConstants.XSD_DATE_TIME))

        assertEquals("2002-05-30T09:30:10Z", literal1.lexicalValue)
        assertEquals("2002-05-30T09:30:10Z", literal1.literalValue.toString())
        assertEquals(from(IRIConstants.XSD_DATE_TIME), literal1.datatypeIRI)

        assertEquals("2002-05-30T03:30:10-06:00", literal2.lexicalValue)
        assertEquals("2002-05-30T09:30:10Z", literal2.literalValue.toString())
        assertEquals(from(IRIConstants.XSD_DATE_TIME), literal2.datatypeIRI)
    }

    should("create DefaultLiteral with custom datatype literal") {
        val literal = literalService.createDefaultLiteral("123", from("http://example.com/datatype"))
        assertEquals("123", literal.lexicalValue)
        assertEquals(from("http://example.com/datatype"), literal.datatypeIRI)
        assertEquals("123", literal.literalValue)
    }

    should("create DefaultLiteral with empty literal") {
        val literal = literalService.createDefaultLiteral("", from(IRIConstants.XSD_INTEGER))
        assertEquals("", literal.lexicalValue)
        assertEquals(from(IRIConstants.XSD_INTEGER), literal.datatypeIRI)
    }

    should("create LanguageTaggedString with valid lang tag") {
        val literal = literalService.createLanguageTaggedString("Hello", "eN-uS")
        assertEquals("Hello", literal.lexicalValue)
        assertEquals("eN-uS", literal.langTag)
        assertEquals("en-US", literal.normalizedLangTag)
        assertEquals(from(IRIConstants.RDF_LANG_STRING_IRI), literal.datatypeIRI)
    }

    should("create LanguageTaggedString with empty lang tag") {
        val literal = literalService.createLanguageTaggedString("Hello", "")
        assertEquals("Hello", literal.lexicalValue)
        assertEquals("", literal.langTag)
        assertEquals("", literal.normalizedLangTag)
        assertEquals(from(IRIConstants.RDF_LANG_STRING_IRI), literal.datatypeIRI)
    }

    should("test equals and hashCode with equal literals") {
        val literal1 = literalService.createDefaultLiteral("123", from(IRIConstants.XSD_INTEGER))
        val literal2 = literalService.createDefaultLiteral("123", from(IRIConstants.XSD_INTEGER))
        assertEquals(literal1, literal2)
        assertEquals(literal1.hashCode(), literal2.hashCode())
    }

    should("test equals and hashCode with different literals") {
        val literal1 = literalService.createDefaultLiteral("123", from(IRIConstants.XSD_INTEGER))
        val literal2 = literalService.createDefaultLiteral("124", from(IRIConstants.XSD_INTEGER))
        assertNotEquals(literal1, literal2)
        assertNotEquals(literal1.hashCode(), literal2.hashCode())
    }

    should("test equals and hashCode with different lexical values") {
        val literal1 = literalService.createDefaultLiteral("0123", from(IRIConstants.XSD_INTEGER))
        val literal2 = literalService.createDefaultLiteral("123", from(IRIConstants.XSD_INTEGER))
        assertNotEquals(literal1, literal2)
        assertNotEquals(literal1.hashCode(), literal2.hashCode())
        assertNotEquals(literal1.lexicalValue, literal2.lexicalValue)
        assertEquals(literal1.literalValue, literal2.literalValue)
    }

    context("createGeneralizedLiteral") {
        should("generalize numeric base datatypes") {
            val literal1 = literalService.createDefaultLiteral("-012", from(IRIConstants.XSD_INTEGER))
            val literal2 = literalService.createDefaultLiteral("-0012", from(IRIConstants.XSD_NegativeInteger))
            val literal3 = literalService.createDefaultLiteral("-00012", from(IRIConstants.XSD_NonPositiveInteger))
            val literal4 = literalService.createDefaultLiteral("-12", from(IRIConstants.XSD_DECIMAL))

            val generalizedLiteral1 = literalService.createGeneralizedLiteral(literal1)
            val generalizedLiteral2 = literalService.createGeneralizedLiteral(literal2)
            val generalizedLiteral3 = literalService.createGeneralizedLiteral(literal3)
            val generalizedLiteral4 = literalService.createGeneralizedLiteral(literal4)

            assertEquals(generalizedLiteral1.datatypeIRI.iri, XSDDatatype.XSDdecimal.uri)
            assertEquals(generalizedLiteral2.datatypeIRI.iri, XSDDatatype.XSDdecimal.uri)
            assertEquals(generalizedLiteral3.datatypeIRI.iri, XSDDatatype.XSDdecimal.uri)
            assertEquals(generalizedLiteral4.datatypeIRI.iri, XSDDatatype.XSDdecimal.uri)

            assertEquals(
                literal4.literalValue,
                generalizedLiteral1.literalValue
            )
            assertEquals(
                literal4.literalValue,
                generalizedLiteral2.literalValue
            )
            assertEquals(
                literal4.literalValue,
                generalizedLiteral3.literalValue
            )
        }
        should("generalize xsd:dayTimeDuration to xsd:duration") {
            val lexical = "P2DT3H4M"
            val literal = literalService.createDefaultLiteral(lexical, from(XSDDatatype.XSDdayTimeDuration.uri))
            val result = literalService.createGeneralizedLiteral(literal)

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDduration.uri
            result.lexicalValue shouldBe "P2DT3H4M"
        }

        should("generalize xsd:yearMonthDuration to xsd:duration") {
            val lexical = "P3Y6M"
            val literal = literalService.createDefaultLiteral(lexical, from(XSDDatatype.XSDyearMonthDuration.uri))
            val result = literalService.createGeneralizedLiteral(literal)

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDduration.uri
            result.lexicalValue shouldBe "P3Y6M"
        }

        should("generalize xsd:dateTimeStamp to xsd:dateTime") {
            val lexical = "2024-03-28T15:00:00Z"
            val literal = literalService.createDefaultLiteral(lexical, from(XSDDatatype.XSDdateTimeStamp.uri))
            val result = literalService.createGeneralizedLiteral(literal)

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDdateTime.uri
            result.lexicalValue shouldBe "2024-03-28T15:00:00Z"
        }

        should("generalize xsd:int to xsd:decimal") {
            val lexical = "42"
            val literal = literalService.createDefaultLiteral(lexical, from(XSDDatatype.XSDint.uri))
            val result = literalService.createGeneralizedLiteral(literal)

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDdecimal.uri
            result.lexicalValue shouldBe "42"
        }

        should("generalize xsd:string to xsd:string") {
            val lexical = "hello"
            val literal = literalService.createDefaultLiteral(lexical, from(XSDDatatype.XSDstring.uri))
            val result = literalService.createGeneralizedLiteral(literal)

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDstring.uri
            result.lexicalValue shouldBe "hello"
        }

        should("generalize and canonicalize xsd:dateTimeStamp to xsd:dateTime") {
            val literal = DefaultLiteral("2024-03-28T15:00:00.000Z", from(XSDDatatype.XSDdateTimeStamp.uri), "2024-03-28T15:00:00.000Z")
            val result = literalService.createGeneralizedLiteral(literal)

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDdateTime.uri
            result.lexicalValue shouldBe "2024-03-28T15:00:00Z"
        }

        should("generalize and canonicalize xsd:int to xsd:decimal") {
            val literal = DefaultLiteral("+042", from(XSDDatatype.XSDint.uri), 42)
            val result = literalService.createGeneralizedLiteral(literal)

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDdecimal.uri
            result.lexicalValue shouldBe "42"
        }

        should("preserve canonical xsd:string") {
            val literal = DefaultLiteral("Hello World", from(XSDDatatype.XSDstring.uri), "Hello World")
            val result = literalService.createGeneralizedLiteral(literal)

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDstring.uri
            result.lexicalValue shouldBe "Hello World"
        }
        should("not throw an exception when generalizing a literal with an unknown datatype") {
            val literal = DefaultLiteral("unknown", from("http://example.com/unknown"), "unknown")
            val result = literalService.createGeneralizedLiteral(literal)

            result.datatypeIRI.iri shouldBe "http://example.com/unknown"
            result.lexicalValue shouldBe "unknown"
        }
    }
})
