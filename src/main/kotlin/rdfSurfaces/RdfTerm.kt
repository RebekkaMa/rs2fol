package rdfSurfaces

import IRIConstants
import org.apache.jena.datatypes.BaseDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype

abstract class RdfTripleElement

data class IRI(val iri: String) : RdfTripleElement() {

    fun getFragment(): String? {
        return iri.substringAfterLast('#').takeIf { it.length != iri.length }
    }

    fun getIRIWithoutFragment(): String {
        return iri.substringBeforeLast('#').let { if (it.length == iri.length) it else "$it#" }
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

data class Collection(val list: List<RdfTripleElement>) : RdfTripleElement()

open class Literal(val literalValue: Any, val datatype: BaseDatatype) : RdfTripleElement() {

    //TODO(support: http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral + http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML
    // || see:https://www.w3.org/TR/rdf11-concepts/#datatype-iris)
    companion object {
        fun fromNonNumericLiteral(lexicalForm: String, datatypeIRI: IRI): Literal {
            val datatype = when {
                datatypeIRI.iri.startsWith(IRIConstants.XSD_IRI) -> when (datatypeIRI.getFragment()) {
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
                    else -> XSDDatatype(datatypeIRI.iri)
                }

                else -> BaseDatatype(datatypeIRI.iri)
            }

            //TODO(Catch exceptions?)
            return Literal(lexicalForm.takeUnless { datatype is XSDDatatype } ?: datatype.parse(lexicalForm), datatype)
        }

        fun fromNonNumericLiteral(lexicalValue: String, langTag: String): LanguageTaggedString =
            LanguageTaggedString(Pair(lexicalValue, langTag.lowercase()))

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

class LanguageTaggedString(lexicalValue: Pair<String, String>) :
    Literal(literalValue = lexicalValue, datatype = BaseDatatype(IRIConstants.RDF_LANG_STRING_IRI)) {
    val lexicalForm = lexicalValue.first
    val languageTag = lexicalValue.second
}