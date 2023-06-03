import org.apache.jena.datatypes.xsd.XSDDatatype.*

class RDFLiteralToTPTPConstantTransformer {

    val xsdIri = "http://www.w3.org/2001/XMLSchema#"

    fun transformNumericLiteral(numericLiteral: String): String {
        return when {
            numericLiteral.contains("E", ignoreCase = true) -> transformLexicalValue(numericLiteral, xsdIri + "double")
            numericLiteral.contains(".", ignoreCase = true) -> transformLexicalValue(numericLiteral, xsdIri + "decimal")
            else -> transformLexicalValue(numericLiteral, xsdIri + "integer")
        }
    }

    fun transformLexicalValue(lexicalValue: String, fullDatatypeIRI: String = xsdIri + "string"): String {
        //TODO(Catch exceptions)

        // https://jena.apache.org/documentation/javadoc/jena/org.apache.jena.core/org/apache/jena/datatypes/xsd/XSDDatatype.html#XSDnegativeInteger
        val result = when {
            fullDatatypeIRI.startsWith(xsdIri) -> when (fullDatatypeIRI.removePrefix(xsdIri)) {
                "string" -> XSDstring.parse(lexicalValue).toString()
                "boolean" -> XSDboolean.parse(lexicalValue).toString()
                "decimal" -> XSDdecimal.parse(lexicalValue).toString()
                "integer" -> XSDinteger.parse(lexicalValue).toString()
                "double" -> XSDdouble.parse(lexicalValue).toString()
                "float" -> XSDfloat.parse(lexicalValue).toString()
                "date" -> XSDdate.parse(lexicalValue).toString()
                "time" -> XSDtime.parse(lexicalValue).toString()
                "dateTime" -> XSDdateTime.parse(lexicalValue).toString()
                "dateTimeStamp" -> XSDdateTimeStamp.parse(lexicalValue).toString()
                "gYear" -> XSDgYear.parse(lexicalValue).toString()
                "gMonth" -> XSDgMonth.parse(lexicalValue).toString()
                "gDay" -> XSDgYear.parse(lexicalValue).toString()
                "gYearMonth" -> XSDgYearMonth.parse(lexicalValue).toString()
                "duration" -> XSDduration.parse(lexicalValue).toString()
                "yearMonthDuration" -> XSDyearMonthDuration.parse(lexicalValue).toString()
                "dayTimeDuration" -> XSDdayTimeDuration.parse(lexicalValue).toString()
                "byte" -> XSDbyte.parse(lexicalValue).toString()
                "short" -> XSDshort.parse(lexicalValue).toString()
                "int" -> XSDint.parse(lexicalValue).toString()
                "long" -> XSDlong.parse(lexicalValue).toString()
                "unsignedByte" -> XSDunsignedByte.parse(lexicalValue).toString()
                "unsignedShort" -> XSDshort.parse(lexicalValue).toString()
                "unsignedInt" -> XSDunsignedInt.parse(lexicalValue).toString()
                "unsignedLong" -> XSDunsignedLong.parse(lexicalValue).toString()
                "positiveInteger" -> XSDpositiveInteger.parse(lexicalValue).toString()
                "nonNegativeInteger" -> XSDnonNegativeInteger.parse(lexicalValue).toString()
                "negativeInteger" -> XSDnegativeInteger.parse(lexicalValue).toString()
                "nonPositiveInteger" -> XSDnonPositiveInteger.parse(lexicalValue).toString()
                "hexBinary" -> XSDhexBinary.parse(lexicalValue).toString()
                "base64Binary" -> XSDbase64Binary.parse(lexicalValue).toString()
                "anyURI" -> XSDanyURI.parse(lexicalValue).toString()
                "language" -> XSDlanguage.parse(lexicalValue).toString()
                "normalizedString" -> XSDnormalizedString.parse(lexicalValue).toString()
                "token" -> XSDtoken.parse(lexicalValue).toString()
                "NMTOKEN" -> XSDNMTOKEN.parse(lexicalValue).toString()
                "Name" -> XSDName.parse(lexicalValue).toString()
                "NCName" -> XSDNCName.parse(lexicalValue).toString()
                else -> lexicalValue
            }.let { "\"$it\"^^$fullDatatypeIRI" }
            fullDatatypeIRI.startsWith("@") -> "\"" + lexicalValue + "\"" + fullDatatypeIRI.lowercase()
            else -> "\"$lexicalValue\"^^$fullDatatypeIRI"

        }
        return "'$result'"
    }


}