package unit.adapter.services.coder

import adapter.jena.XSDLiteralServiceImpl
import app.interfaces.results.XSDLiteralServiceResult
import entities.rdfsurfaces.rdf_term.DefaultLiteral
import entities.rdfsurfaces.rdf_term.IRI.Companion.from
import entities.rdfsurfaces.rdf_term.LanguageTaggedString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.apache.jena.datatypes.xsd.XSDDatatype
import util.IRIConstants
import util.commandResult.getErrorOrNull
import util.commandResult.getSuccessOrNull
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LiteralServiceTest : ShouldSpec({

    val literalService = XSDLiteralServiceImpl()
    should("canonicalize date time literals") {
        val literal1 = DefaultLiteral("2002-05-30T09:30:10Z", from(IRIConstants.XSD_DATE_TIME))
        val literal2 =
            DefaultLiteral("2002-05-30T03:30:10-06:00", from(IRIConstants.XSD_DATE_TIME))

        val generalized1 = literalService.createGeneralizedLiteral(literal1).getSuccessOrNull()?.literal
        val generalized2 = literalService.createGeneralizedLiteral(literal2).getSuccessOrNull()?.literal

        assertEquals("2002-05-30T09:30:10Z", generalized1?.lexicalValue)
        assertEquals(from(IRIConstants.XSD_DATE_TIME), generalized1?.datatypeIRI)

        assertEquals("2002-05-30T09:30:10Z", generalized2?.lexicalValue)
        assertEquals(from(IRIConstants.XSD_DATE_TIME), generalized2?.datatypeIRI)
    }

    should("not canonicalize custom data type literals") {
        val literal = DefaultLiteral("123", from("http://example.com/datatype"))
        val canonicalizedLiteral = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal

        assertEquals("123", canonicalizedLiteral?.lexicalValue)
        assertEquals(from("http://example.com/datatype"), canonicalizedLiteral?.datatypeIRI)
    }

    should("return error if lexical values is invalid") {
        val literal = DefaultLiteral("", from(IRIConstants.XSD_INTEGER))
        val canonicalizedLiteral = literalService.createGeneralizedLiteral(literal).getErrorOrNull()

        canonicalizedLiteral.shouldBeTypeOf<XSDLiteralServiceResult.Error.InvalidLexicalValue>()
    }

    should("canonicalize language tagged string") {
        val literal = LanguageTaggedString("Hello", "eN-uS")
        val canonicalizedLiteral = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal

        canonicalizedLiteral.shouldBeTypeOf<LanguageTaggedString>()

        assertEquals("Hello", canonicalizedLiteral.lexicalValue)
        assertEquals("en-US", canonicalizedLiteral.langTag)
        assertEquals(from(IRIConstants.RDF_LANG_STRING_IRI), canonicalizedLiteral.datatypeIRI)
    }

    should("canonicalize invalid language tagged literal") {
        val literal = LanguageTaggedString("Hello", "")
        val canonicalizedLiteral = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal
        canonicalizedLiteral.shouldBeTypeOf<LanguageTaggedString>()

        assertEquals("Hello", canonicalizedLiteral.lexicalValue)
        assertEquals("", canonicalizedLiteral.langTag)
        assertEquals(from(IRIConstants.RDF_LANG_STRING_IRI), canonicalizedLiteral.datatypeIRI)
    }

    should("test equals and hashCode with equal literals") {
        val literal1 = DefaultLiteral("123", from(IRIConstants.XSD_INTEGER))
        val literal2 = DefaultLiteral("123", from(IRIConstants.XSD_INTEGER))
        assertEquals(literal1, literal2)
        assertEquals(literal1.hashCode(), literal2.hashCode())
    }

    should("test equals and hashCode with different literals") {
        val literal1 = DefaultLiteral("123", from(IRIConstants.XSD_INTEGER))
        val literal2 = DefaultLiteral("124", from(IRIConstants.XSD_INTEGER))
        assertNotEquals(literal1, literal2)
        assertNotEquals(literal1.hashCode(), literal2.hashCode())
    }

    context("createGeneralizedLiteral") {
        should("generalize numeric base datatypes") {
            val literal1 = DefaultLiteral("-012", from(IRIConstants.XSD_INTEGER))
            val literal2 = DefaultLiteral("-0012", from(IRIConstants.XSD_NegativeInteger))
            val literal3 = DefaultLiteral("-00012", from(IRIConstants.XSD_NonPositiveInteger))
            val literal4 = DefaultLiteral("-12", from(IRIConstants.XSD_DECIMAL))

            val generalizedLiteral1 = literalService.createGeneralizedLiteral(literal1).getSuccessOrNull()?.literal.shouldNotBeNull()
            val generalizedLiteral2 = literalService.createGeneralizedLiteral(literal2).getSuccessOrNull()?.literal.shouldNotBeNull()
            val generalizedLiteral3 = literalService.createGeneralizedLiteral(literal3).getSuccessOrNull()?.literal.shouldNotBeNull()
            val generalizedLiteral4 = literalService.createGeneralizedLiteral(literal4).getSuccessOrNull()?.literal.shouldNotBeNull()

            assertEquals(generalizedLiteral1.datatypeIRI.iri, XSDDatatype.XSDdecimal.uri)
            assertEquals(generalizedLiteral2.datatypeIRI.iri, XSDDatatype.XSDdecimal.uri)
            assertEquals(generalizedLiteral3.datatypeIRI.iri, XSDDatatype.XSDdecimal.uri)
            assertEquals(generalizedLiteral4.datatypeIRI.iri, XSDDatatype.XSDdecimal.uri)


            assertEquals(
                generalizedLiteral4,
                generalizedLiteral1
            )
            assertEquals(
                generalizedLiteral4,
                generalizedLiteral2
            )
            assertEquals(
                generalizedLiteral4,
                generalizedLiteral3
            )

        }

        should("generalize xsd:dayTimeDuration to xsd:duration") {
            val lexical = "P2DT3H4M"
            val literal = DefaultLiteral(lexical, from(XSDDatatype.XSDdayTimeDuration.uri))
            val result = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal.shouldNotBeNull()

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDduration.uri
            result.lexicalValue shouldBe "P2DT3H4M"
        }

        should("generalize xsd:yearMonthDuration to xsd:duration") {
            val lexical = "P3Y6M"
            val literal = DefaultLiteral(lexical, from(XSDDatatype.XSDyearMonthDuration.uri))
            val result = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal.shouldNotBeNull()

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDduration.uri
            result.lexicalValue shouldBe "P3Y6M"
        }

        should("generalize xsd:dateTimeStamp to xsd:dateTime") {
            val lexical = "2024-03-28T15:00:00Z"
            val literal = DefaultLiteral(lexical, from(XSDDatatype.XSDdateTimeStamp.uri))
            val result = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal.shouldNotBeNull()

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDdateTime.uri
            result.lexicalValue shouldBe "2024-03-28T15:00:00Z"
        }

        should("generalize xsd:int to xsd:decimal") {
            val lexical = "42"
            val literal = DefaultLiteral(lexical, from(XSDDatatype.XSDint.uri))
            val result = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal.shouldNotBeNull()

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDdecimal.uri
            result.lexicalValue shouldBe "42"
        }

        should("generalize xsd:string to xsd:string") {
            val lexical = "hello"
            val literal = DefaultLiteral(lexical, from(XSDDatatype.XSDstring.uri))
            val result = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal.shouldNotBeNull()

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDstring.uri
            result.lexicalValue shouldBe "hello"
        }

        should("generalize and canonicalize xsd:dateTimeStamp to xsd:dateTime") {
            val literal = DefaultLiteral("2024-03-28T15:00:00.000Z", from(XSDDatatype.XSDdateTimeStamp.uri))
            val result = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal.shouldNotBeNull()

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDdateTime.uri
            result.lexicalValue shouldBe "2024-03-28T15:00:00Z"
        }

        should("generalize and canonicalize xsd:int to xsd:decimal") {
            val literal = DefaultLiteral("+042", from(XSDDatatype.XSDint.uri))
            val result = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal.shouldNotBeNull()

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDdecimal.uri
            result.lexicalValue shouldBe "42"
        }

        should("preserve canonical xsd:string") {
            val literal = DefaultLiteral("Hello World", from(XSDDatatype.XSDstring.uri))
            val result = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal.shouldNotBeNull()

            result.datatypeIRI.iri shouldBe XSDDatatype.XSDstring.uri
            result.lexicalValue shouldBe "Hello World"
        }
        should("not throw an exception when generalizing a literal with an unknown datatype") {
            val literal = DefaultLiteral("unknown", from("http://example.com/unknown"))
            val result = literalService.createGeneralizedLiteral(literal).getSuccessOrNull()?.literal.shouldNotBeNull()

            result.datatypeIRI.iri shouldBe "http://example.com/unknown"
            result.lexicalValue shouldBe "unknown"
        }
    }
})
