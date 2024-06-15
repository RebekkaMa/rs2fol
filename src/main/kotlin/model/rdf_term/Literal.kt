package model.rdf_term

import org.apache.jena.datatypes.BaseDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype.*
import model.rdf_term.IRI.Companion.from
import util.IRIConstants

sealed class Literal : RdfTerm {
    abstract val literalValue: Any
    abstract val datatype: BaseDatatype

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is Literal -> other.literalValue == literalValue && other.datatype.uri == datatype.uri
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = literalValue.hashCode()
        result = 31 * result + datatype.uri.hashCode()
        return result
    }
}

data class DefaultLiteral(override val literalValue: Any, override val datatype: BaseDatatype) : Literal() {

    companion object {

        fun fromNonNumericLiteral(
            lexicalForm: String,
            datatypeIRI: IRI
        ): Literal {
            val datatype = when {
                datatypeIRI.iri.startsWith(IRIConstants.XSD_IRI) -> when (datatypeIRI.fragment) {
                    "string" -> XSDstring
                    "boolean" -> XSDboolean
                    "decimal" -> XSDdecimal
                    "integer" -> XSDinteger
                    "double" -> XSDdouble
                    "float" -> XSDfloat
                    "date" -> XSDdate
                    "time" -> XSDtime
                    "dateTime" -> XSDdateTime
                    "dateTimeStamp" -> XSDdateTimeStamp
                    "gYear" -> XSDgYear
                    "gMonth" -> XSDgMonth
                    "gDay" -> XSDgYear
                    "gYearMonth" -> XSDgYearMonth
                    "duration" -> XSDduration
                    "yearMonthDuration" -> XSDyearMonthDuration
                    "dayTimeDuration" -> XSDdayTimeDuration
                    "byte" -> XSDbyte
                    "short" -> XSDshort
                    "int" -> XSDint
                    "long" -> XSDlong
                    "unsignedByte" -> XSDunsignedByte
                    "unsignedShort" -> XSDshort
                    "unsignedInt" -> XSDunsignedInt
                    "unsignedLong" -> XSDunsignedLong
                    "positiveInteger" -> XSDpositiveInteger
                    "nonNegativeInteger" -> XSDnonNegativeInteger
                    "negativeInteger" -> XSDnegativeInteger
                    "nonPositiveInteger" -> XSDnonPositiveInteger
                    "hexBinary" -> XSDhexBinary
                    "base64Binary" -> XSDbase64Binary
                    "anyURI" -> XSDanyURI
                    "language" -> XSDlanguage
                    "normalizedString" -> XSDnormalizedString
                    "token" -> XSDtoken
                    "NMTOKEN" -> XSDNMTOKEN
                    "Name" -> XSDName
                    "NCName" -> XSDNCName
                    else -> BaseDatatype(datatypeIRI.iri)
                }

                else -> BaseDatatype(datatypeIRI.iri)
            }
            return DefaultLiteral(
                runCatching {
                    lexicalForm.takeUnless { datatype is XSDDatatype } ?: datatype.parse(lexicalForm)
                }.getOrDefault(lexicalForm),
                datatype
            )
        }

        fun fromNumericLiteral(numericLiteral: String): Literal =
            when {
                numericLiteral.contains(
                    "E",
                    ignoreCase = true
                ) -> fromNonNumericLiteral(
                    numericLiteral,
                    from(IRIConstants.XSD_DOUBLE)
                )

                numericLiteral.contains(
                    ".",
                    ignoreCase = true
                ) -> fromNonNumericLiteral(
                    numericLiteral,
                    from(IRIConstants.XSD_DECIMAL)
                )

                else -> fromNonNumericLiteral(
                    numericLiteral,
                    from(IRIConstants.XSD_INTEGER)
                )
            }

    }
}

data class LanguageTaggedString(val lexicalForm: String, val languageTag: String) : Literal() {
    override val literalValue = lexicalForm to languageTag
    override val datatype = BaseDatatype(IRIConstants.RDF_LANG_STRING_IRI)
}