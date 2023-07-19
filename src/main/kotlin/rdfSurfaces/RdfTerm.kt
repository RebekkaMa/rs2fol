package rdfSurfaces

import IRIConstants
import org.apache.jena.datatypes.BaseDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype

abstract class RdfTripleElement

data class IRI(val iri: String) : RdfTripleElement() {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is IRI -> iri == other.iri
            else -> false
        }
    }

    override fun hashCode(): Int {
        return iri.hashCode()
    }


}

data class BlankNode(val blankNodeId: String) : RdfTripleElement() {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is BlankNode -> blankNodeId.equals(other.blankNodeId, ignoreCase = true)
            else -> false
        }
    }

    override fun hashCode(): Int {
        return blankNodeId.hashCode()
    }
}

data class Collection(val list: List<RdfTripleElement>): RdfTripleElement()

class Literal(val lexicalValue: Any, val xsdDatatype: BaseDatatype) : RdfTripleElement() {

    companion object {
        fun fromNonNumericLiteral(lexicalValueString: String, dataTypeIRI: IRI): Literal {
            val xsdDatatype = when {
                dataTypeIRI.iri.startsWith(IRIConstants.XSD_IRI) -> when (dataTypeIRI.iri.removePrefix(IRIConstants.XSD_IRI)) {
                    "string" -> XSDDatatype.XSDstring
                    "boolean" -> XSDDatatype.XSDboolean
                    "decimal" -> XSDDatatype.XSDdecimal
                    "integer" -> XSDDatatype.XSDinteger
                    "double" -> XSDDatatype.XSDdouble
                    "float" -> XSDDatatype.XSDfloat
                    "date" -> XSDDatatype.XSDdate
                    "time" -> XSDDatatype.XSDtime
                    "dateTime" -> XSDDatatype.XSDdateTime
                    "dateTimeStamp" -> XSDDatatype.XSDdateTimeStamp
                    "gYear" -> XSDDatatype.XSDgYear
                    "gMonth" -> XSDDatatype.XSDgMonth
                    "gDay" -> XSDDatatype.XSDgYear
                    "gYearMonth" -> XSDDatatype.XSDgYearMonth
                    "duration" -> XSDDatatype.XSDduration
                    "yearMonthDuration" -> XSDDatatype.XSDyearMonthDuration
                    "dayTimeDuration" -> XSDDatatype.XSDdayTimeDuration
                    "byte" -> XSDDatatype.XSDbyte
                    "short" -> XSDDatatype.XSDshort
                    "int" -> XSDDatatype.XSDint
                    "long" -> XSDDatatype.XSDlong
                    "unsignedByte" -> XSDDatatype.XSDunsignedByte
                    "unsignedShort" -> XSDDatatype.XSDshort
                    "unsignedInt" -> XSDDatatype.XSDunsignedInt
                    "unsignedLong" -> XSDDatatype.XSDunsignedLong
                    "positiveInteger" -> XSDDatatype.XSDpositiveInteger
                    "nonNegativeInteger" -> XSDDatatype.XSDnonNegativeInteger
                    "negativeInteger" -> XSDDatatype.XSDnegativeInteger
                    "nonPositiveInteger" -> XSDDatatype.XSDnonPositiveInteger
                    "hexBinary" -> XSDDatatype.XSDhexBinary
                    "base64Binary" -> XSDDatatype.XSDbase64Binary
                    "anyURI" -> XSDDatatype.XSDanyURI
                    "language" -> XSDDatatype.XSDlanguage
                    "normalizedString" -> XSDDatatype.XSDnormalizedString
                    "token" -> XSDDatatype.XSDtoken
                    "NMTOKEN" -> XSDDatatype.XSDNMTOKEN
                    "Name" -> XSDDatatype.XSDName
                    "NCName" -> XSDDatatype.XSDNCName
                    else -> XSDDatatype(dataTypeIRI.iri)
                }

                else -> BaseDatatype(dataTypeIRI.iri)
            }
            //TODO(Catch exceptions)
            // https://jena.apache.org/documentation/javadoc/jena/org.apache.jena.core/org/apache/jena/datatypes/xsd/XSDDatatype.html#XSDnegativeInteger
            val lexicalValue = xsdDatatype.parse(lexicalValueString)
            return Literal(lexicalValue, xsdDatatype)
        }

        fun fromNonNumericLiteral(lexicalValue: String, langTag: String): Literal =
            Literal(Pair(lexicalValue, langTag.lowercase()), BaseDatatype(IRIConstants.RDF_LANG_STRING_IRI))


        fun fromNumericLiteral(numericLiteral: String): Literal {
            return when {
                numericLiteral.contains("E", ignoreCase = true) -> fromNonNumericLiteral(
                    numericLiteral,
                    IRI(IRIConstants.XSD_DOUBLE)
                )

                numericLiteral.contains(".", ignoreCase = true) -> fromNonNumericLiteral(
                    numericLiteral,
                    IRI(IRIConstants.XSD_DECIMAL)
                )

                else -> fromNonNumericLiteral(numericLiteral, IRI(IRIConstants.XSD_INTEGER))
            }
        }
    }
}