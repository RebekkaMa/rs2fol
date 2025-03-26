package adapter.services.coder

import adapter.jena.LiteralServiceImpl
import entities.rdfsurfaces.rdf_term.IRI
import io.kotest.core.spec.style.ShouldSpec
import org.apache.jena.datatypes.xsd.XSDDatatype
import util.IRIConstants
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LiteralServiceTest : ShouldSpec({

    val literalService = LiteralServiceImpl()
    should("create DefaultLiteral with date time literals") {
        val literal1 = literalService.createDefaultLiteral("2002-05-30T09:30:10Z", IRI.from(IRIConstants.XSD_DATE_TIME))
        val literal2 =
            literalService.createDefaultLiteral("2002-05-30T03:30:10-06:00", IRI.from(IRIConstants.XSD_DATE_TIME))

        assertEquals("2002-05-30T09:30:10Z", literal1.lexicalValue)
        assertEquals("2002-05-30T09:30:10Z", literal1.literalValue.toString())
        assertEquals(IRI.from(IRIConstants.XSD_DATE_TIME), literal1.datatypeIRI)

        assertEquals("2002-05-30T03:30:10-06:00", literal2.lexicalValue)
        assertEquals("2002-05-30T09:30:10Z", literal2.literalValue.toString())
        assertEquals(IRI.from(IRIConstants.XSD_DATE_TIME), literal2.datatypeIRI)
    }

    should("create DefaultLiteral with custom datatype literal") {
        val literal = literalService.createDefaultLiteral("123", IRI.from("http://example.com/datatype"))
        assertEquals("123", literal.lexicalValue)
        assertEquals(IRI.from("http://example.com/datatype"), literal.datatypeIRI)
        assertEquals("123", literal.literalValue)
    }

    should("create DefaultLiteral with empty literal") {
        val literal = literalService.createDefaultLiteral("", IRI.from(IRIConstants.XSD_INTEGER))
        assertEquals("", literal.lexicalValue)
        assertEquals(IRI.from(IRIConstants.XSD_INTEGER), literal.datatypeIRI)
    }

    should("create LanguageTaggedString with valid lang tag") {
        val literal = literalService.createLanguageTaggedString("Hello", "eN-uS")
        assertEquals("Hello", literal.lexicalValue)
        assertEquals("eN-uS", literal.langTag)
        assertEquals("en-US", literal.normalizedLangTag)
        assertEquals(IRI.from(IRIConstants.RDF_LANG_STRING_IRI), literal.datatypeIRI)
    }

    should("create LanguageTaggedString with empty lang tag") {
        val literal = literalService.createLanguageTaggedString("Hello", "")
        assertEquals("Hello", literal.lexicalValue)
        assertEquals("", literal.langTag)
        assertEquals("", literal.normalizedLangTag)
        assertEquals(IRI.from(IRIConstants.RDF_LANG_STRING_IRI), literal.datatypeIRI)
    }

    should("test equals and hashCode with equal literals") {
        val literal1 = literalService.createDefaultLiteral("123", IRI.from(IRIConstants.XSD_INTEGER))
        val literal2 = literalService.createDefaultLiteral("123", IRI.from(IRIConstants.XSD_INTEGER))
        assertEquals(literal1, literal2)
        assertEquals(literal1.hashCode(), literal2.hashCode())
    }

    should("test equals and hashCode with different literals") {
        val literal1 = literalService.createDefaultLiteral("123", IRI.from(IRIConstants.XSD_INTEGER))
        val literal2 = literalService.createDefaultLiteral("124", IRI.from(IRIConstants.XSD_INTEGER))
        assertNotEquals(literal1, literal2)
        assertNotEquals(literal1.hashCode(), literal2.hashCode())
    }

    should("test equals and hashCode with different lexical values") {
        val literal1 = literalService.createDefaultLiteral("0123", IRI.from(IRIConstants.XSD_INTEGER))
        val literal2 = literalService.createDefaultLiteral("123", IRI.from(IRIConstants.XSD_INTEGER))
        assertNotEquals(literal1, literal2)
        assertNotEquals(literal1.hashCode(), literal2.hashCode())
        assertNotEquals(literal1.lexicalValue, literal2.lexicalValue)
        assertEquals(literal1.literalValue, literal2.literalValue)
    }

    context("createGeneralizedLiteral") {
        should("generalize numeric base datatypes") {
            val literal1 = literalService.createDefaultLiteral("-012", IRI.from(IRIConstants.XSD_INTEGER))
            val literal2 = literalService.createDefaultLiteral("-0012", IRI.from(IRIConstants.XSD_NegativeInteger))
            val literal3 = literalService.createDefaultLiteral("-00012", IRI.from(IRIConstants.XSD_NonPositiveInteger))
            val literal4 = literalService.createDefaultLiteral("-12", IRI.from(IRIConstants.XSD_DECIMAL))

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
        should("generalize string base datatypes") {
        }
    }

})
