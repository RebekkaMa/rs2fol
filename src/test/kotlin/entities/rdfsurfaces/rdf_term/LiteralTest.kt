package entities.rdfsurfaces.rdf_term

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import util.IRIConstants

class LiteralTest {

    @Test
    fun createDefaultLiteral_withValidDoubleLiteral() {
        val literal = DefaultLiteral.fromNumericLiteral("1.23E4")
        assertEquals("1.23E4", literal.lexicalValue)
        assertEquals(IRI.from(IRIConstants.XSD_DOUBLE), literal.datatypeIRI)
    }

    @Test
    fun createDefaultLiteral_withValidDecimalLiteral() {
        val literal = DefaultLiteral.fromNumericLiteral("123.45")
        assertEquals("123.45", literal.lexicalValue)
        assertEquals(IRI.from(IRIConstants.XSD_DECIMAL), literal.datatypeIRI)
    }

    @Test
    fun createDefaultLiteral_withValidIntegerLiteral() {
        val literal = DefaultLiteral.fromNumericLiteral("123")
        assertEquals("123", literal.lexicalValue)
        assertEquals(IRI.from(IRIConstants.XSD_INTEGER), literal.datatypeIRI)
    }

    @Test
    fun createDefaultLiteral_withDateTimeLiterals() {
        val literal1 = DefaultLiteral("2002-05-30T09:30:10Z", IRI.from(IRIConstants.XSD_DATE_TIME))
        val literal2 = DefaultLiteral("2002-05-30T03:30:10-06:00", IRI.from(IRIConstants.XSD_DATE_TIME))

        assertEquals("2002-05-30T09:30:10Z", literal1.lexicalValue)
        assertEquals("2002-05-30T09:30:10Z", literal1.literalValue.toString())
        assertEquals(IRI.from(IRIConstants.XSD_DATE_TIME), literal1.datatypeIRI)

        assertEquals("2002-05-30T03:30:10-06:00", literal2.lexicalValue)
        assertEquals("2002-05-30T09:30:10Z", literal2.literalValue.toString())
        assertEquals(IRI.from(IRIConstants.XSD_DATE_TIME), literal2.datatypeIRI)
    }

    @Test
    fun createDefaultLiteral_withCustomDatatypeLiteral() {
        val literal = DefaultLiteral("123", IRI.from("http://example.com/datatype"))
        assertEquals("123", literal.lexicalValue)
        assertEquals(IRI.from("http://example.com/datatype"), literal.datatypeIRI)
        assertEquals("123", literal.literalValue)
    }

    @Test
    fun createDefaultLiteral_withEmptyLiteral() {
        val literal = DefaultLiteral.fromNumericLiteral("")
        assertEquals("", literal.lexicalValue)
        assertEquals(IRI.from(IRIConstants.XSD_INTEGER), literal.datatypeIRI)
    }

    @Test
    fun createLanguageTaggedString_withValidLangTag() {
        val literal = LanguageTaggedString("Hello", "eN-uS")
        assertEquals("Hello", literal.lexicalValue)
        assertEquals("eN-uS", literal.langTag)
        assertEquals("en-US", literal.normalizedLangTag)
        assertEquals(IRI.from(IRIConstants.RDF_LANG_STRING_IRI), literal.datatypeIRI)
    }

    @Test
    fun createLanguageTaggedString_withEmptyLangTag() {
        val literal = LanguageTaggedString("Hello", "")
        assertEquals("Hello", literal.lexicalValue)
        assertEquals("", literal.langTag)
        assertEquals("", literal.normalizedLangTag)
        assertEquals(IRI.from(IRIConstants.RDF_LANG_STRING_IRI), literal.datatypeIRI)
    }

    @Test
    fun equalsAndHashCode_withEqualLiterals() {
        val literal1 = DefaultLiteral("123", IRI.from(IRIConstants.XSD_INTEGER))
        val literal2 = DefaultLiteral("123", IRI.from(IRIConstants.XSD_INTEGER))
        assertEquals(literal1, literal2)
        assertEquals(literal1.hashCode(), literal2.hashCode())
    }

    @Test
    fun equalsAndHashCode_withDifferentLiterals() {
        val literal1 = DefaultLiteral("123", IRI.from(IRIConstants.XSD_INTEGER))
        val literal2 = DefaultLiteral("124", IRI.from(IRIConstants.XSD_INTEGER))
        assertNotEquals(literal1, literal2)
        assertNotEquals(literal1.hashCode(), literal2.hashCode())
    }

    @Test
    fun equalsAndHashCode_withDifferentLexicalValues() {
        val literal1 = DefaultLiteral("0123", IRI.from(IRIConstants.XSD_INTEGER))
        val literal2 = DefaultLiteral("123", IRI.from(IRIConstants.XSD_INTEGER))
        assertNotEquals(literal1, literal2)
        assertNotEquals(literal1.hashCode(), literal2.hashCode())
        assertNotEquals(literal1.lexicalValue, literal2.lexicalValue)
        assertEquals(literal1.literalValue, literal2.literalValue)
    }
}
