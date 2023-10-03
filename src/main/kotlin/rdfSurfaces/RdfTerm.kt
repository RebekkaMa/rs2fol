package rdfSurfaces

import org.apache.jena.datatypes.BaseDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype.*
import util.IRIConstants

sealed class RdfTripleElement

data class IRI(
    val scheme: String? = null,
    val authority: String? = null,
    val path: String,
    val query: String? = null,
    val fragment: String? = null,
) : RdfTripleElement() {

    val iri: String = componentRecomposition(scheme, authority, path, query, fragment)

    companion object {

        fun from(fullIRI: String): IRI {
            return "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?".toRegex().matchEntire(fullIRI)
                ?.let { matchResult ->
                    IRI(
                        matchResult.destructured.component2()
                            .takeUnless { matchResult.destructured.component1().isEmpty() },
                        matchResult.destructured.component4()
                            .takeUnless { matchResult.destructured.component3().isEmpty() },
                        matchResult.destructured.component5(),
                        matchResult.destructured.component7()
                            .takeUnless { matchResult.destructured.component6().isEmpty() },
                        matchResult.destructured.component9()
                            .takeUnless { matchResult.destructured.component8().isEmpty() }
                    )
                } ?: IRI(path = fullIRI)
        }

        fun componentRecomposition(
            scheme: String? = null,
            authority: String? = null,
            path: String,
            query: String? = null,
            fragment: String? = null,
        ) =
            buildString {
                if (scheme != null) append("$scheme:")
                if (authority != null) append("//$authority")
                append(path)
                if (query != null) append("?$query")
                if (fragment != null) append("#$fragment")
            }

        fun transformReference(R: IRI, B: IRI): IRI {
            fun merge(): String =
                if (B.authority != null && B.path.isEmpty()) "/${R.path}" else (B.path.dropLastWhile { it != '/' } + R.path)

            fun removeDotSegments(path: String): String {

                val inputBuffer = StringBuilder(path)
                val outputBuffer = StringBuilder()

                while (inputBuffer.isNotBlank()) {
                    when {
                        inputBuffer.startsWith("../") -> inputBuffer.deleteRange(0, 3)
                        inputBuffer.startsWith("./") -> inputBuffer.deleteRange(0, 2)
                        inputBuffer.startsWith("/./") -> inputBuffer.setRange(0, 3, "/")
                        inputBuffer.contentEquals("/.") -> inputBuffer.setRange(0, 2, "/")
                        inputBuffer.startsWith("/../") -> {
                            inputBuffer.setRange(0, 4, "/")
                            outputBuffer.delete(outputBuffer.indexOfLast { it == '/' }.takeUnless { it == -1 } ?: 0,
                                outputBuffer.lastIndex + 1)
                        }

                        inputBuffer.contentEquals("/..") -> {
                            inputBuffer.setRange(0, 3, "/")
                            outputBuffer.delete(outputBuffer.indexOfLast { it == '/' }.takeUnless { it == -1 } ?: 0,
                                outputBuffer.lastIndex + 1)
                        }

                        inputBuffer.contentEquals("..") || inputBuffer.contentEquals(".") -> inputBuffer.clear()
                        else -> {
                            val switch =
                                "^(/?[^/]*)(/|$)".toRegex().find(inputBuffer)?.groups?.get(1)?.range
                            if (switch != null) {
                                outputBuffer.append(inputBuffer, switch.first, switch.last + 1)
                                inputBuffer.delete(switch.first, switch.last + 1)
                            }
                        }
                    }
                }
                return outputBuffer.toString()
            }

            val targetURIScheme: String?
            val targetURIauthority: String?
            val targetURIpath: String?
            val targetURIquery: String?

            if (R.scheme != null) {
                targetURIScheme = R.scheme
                targetURIauthority = R.authority
                targetURIpath = removeDotSegments(R.path)
                targetURIquery = R.query
            } else {
                if (R.authority != null) {
                    targetURIauthority = R.authority
                    targetURIpath = removeDotSegments(R.path)
                    targetURIquery = R.query
                } else {
                    if (R.path.isEmpty()) {
                        targetURIpath = B.path
                        targetURIquery = R.query ?: B.query
                    } else {
                        targetURIpath = removeDotSegments(R.path.takeIf { R.path.startsWith("/") } ?: merge())
                        targetURIquery = R.query
                    }
                    targetURIauthority = B.authority
                }
                targetURIScheme = B.scheme
            }

            val targetURIfragment: String? = R.fragment

            return IRI(targetURIScheme, targetURIauthority, targetURIpath, targetURIquery, targetURIfragment)
        }
    }

    fun getIRIWithoutFragment(): String {
        val index = iri.lastIndexOf('#')
        return if (index == -1) iri else iri.substring(0, index + 1)
    }

    fun isRelativeReference() = scheme.isNullOrBlank()

    override fun toString(): String = iri

}

data class BlankNode(val blankNodeId: String) : RdfTripleElement()

data class Collection(val list: List<RdfTripleElement> = listOf()) : RdfTripleElement(), List<RdfTripleElement> by list

open class Literal(val literalValue: Any, val datatype: BaseDatatype) : RdfTripleElement() {

    companion object {
        fun fromNonNumericLiteral(lexicalForm: String, datatypeIRI: IRI): Literal {
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

            return Literal(
                runCatching {
                    lexicalForm.takeUnless { datatype is XSDDatatype } ?: datatype.parse(lexicalForm)
                }.getOrDefault(lexicalForm),
                datatype
            )
        }

        fun fromNonNumericLiteral(lexicalValue: String, langTag: String): LanguageTaggedString =
            LanguageTaggedString(Pair(lexicalValue, langTag.lowercase()))

        fun fromNumericLiteral(numericLiteral: String): Literal =
            when {
                numericLiteral.contains("E", ignoreCase = true) -> fromNonNumericLiteral(
                    numericLiteral,
                    IRI.from(IRIConstants.XSD_DOUBLE)
                )

                numericLiteral.contains(".", ignoreCase = true) -> fromNonNumericLiteral(
                    numericLiteral,
                    IRI.from(IRIConstants.XSD_DECIMAL)
                )

                else -> fromNonNumericLiteral(numericLiteral, IRI.from(IRIConstants.XSD_INTEGER))
            }

    }

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


class LanguageTaggedString(lexicalValue: Pair<String, String>) :
    Literal(literalValue = lexicalValue, datatype = BaseDatatype(IRIConstants.RDF_LANG_STRING_IRI)) {
    val lexicalForm = lexicalValue.first
    val languageTag = lexicalValue.second
}