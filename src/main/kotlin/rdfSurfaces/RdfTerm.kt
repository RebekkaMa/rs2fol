package rdfSurfaces

import IRIConstants
import org.apache.jena.datatypes.BaseDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype

abstract class RdfTripleElement

data class IRI(
    val scheme: String? = null,
    val authority: String? = null,
    val path: String,
    val query: String? = null,
    val fragment: String? = null,
) : RdfTripleElement() {

    val iri: String = componentRecomposition(scheme, authority, path, query, fragment)

    companion object {
        //TODO()
        fun from(fullIRI: String): IRI {
            return "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?".toRegex().matchEntire(fullIRI)
                ?.let { matchResult ->
                    IRI(
                        if (matchResult.destructured.component1()
                                .isEmpty()
                        ) null else matchResult.destructured.component2(),
                        if (matchResult.destructured.component3()
                                .isEmpty()
                        ) null else matchResult.destructured.component4(),
                        matchResult.destructured.component5(),
                        if (matchResult.destructured.component6()
                                .isEmpty()
                        ) null else matchResult.destructured.component7(),
                        if (matchResult.destructured.component8()
                                .isEmpty()
                        ) null else matchResult.destructured.component9(),
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
                if (B.authority != null && B.path == "") "/${R.path}" else {
                    B.path.dropLastWhile { it != '/' } + R.path
                }

            fun removeDotSegments(path: String): String {
                //TODO(Use more efficient StringBuilder )
//                var inputBuffer = path
//                var outputBuffer = ""
//
//                while (inputBuffer.isNotBlank()) {
//                    when {
//                        inputBuffer.startsWith("../") || inputBuffer.startsWith("./")
//                        -> inputBuffer = inputBuffer.replace("^((\\.\\./)|(\\./))".toRegex(), "")
//
//                        inputBuffer.startsWith("/./") || inputBuffer == "/."
//                        -> inputBuffer = inputBuffer.replace("^((/\\./)|(/\\.$))".toRegex(), "/")
//
//                        inputBuffer.startsWith("/../") || inputBuffer == "/.."
//                        -> {
//                            inputBuffer = inputBuffer.replace("^((/\\.\\./)|(/\\.\\.$))".toRegex(), "/")
//                            outputBuffer = outputBuffer.dropLastWhile { it != '/' }.removeSuffix("/")
//                        }
//
//                        inputBuffer == ".." || inputBuffer == "." -> inputBuffer = ""
//                        else -> {
//                            val switch = "^(/?[^/]*)(/|$)".toRegex().find(inputBuffer)?.destructured?.component1().orEmpty()
//
//                            outputBuffer += switch
//                            inputBuffer = inputBuffer.removePrefix(switch)
//                        }
//                    }
//                }

                val inputBuffer = StringBuilder(path)
                val outputBuffer = StringBuilder()

                while (inputBuffer.isNotBlank()) {
                    when {
                        inputBuffer.startsWith("../") || inputBuffer.startsWith("./")
                        -> {
                            val range = "^((\\.\\./)|(\\./))".toRegex().find(inputBuffer)?.range
                            if (range != null) inputBuffer.deleteRange(range.first, range.last + 1)
                        }

                        inputBuffer.startsWith("/./") || inputBuffer.toString() == "/."
                        -> {
                            val range = "^((/\\./)|(/\\.$))".toRegex().find(inputBuffer)?.range
                            if (range != null) inputBuffer.replace(range.first, range.last + 1, "/")
                        }

                        inputBuffer.startsWith("/../") || inputBuffer.toString() == "/.."
                        -> {
                            val range = "^((/\\.\\./)|(/\\.\\.$))".toRegex().find(inputBuffer)?.range
                            if (range != null) {
                                inputBuffer.replace(range.first, range.last + 1, "/")
                                outputBuffer.delete(outputBuffer.indexOfLast { it == '/' }.takeUnless { it == -1 } ?: 0,
                                    outputBuffer.lastIndex + 1)
                            }
                        }

                        inputBuffer.toString() == ".." || inputBuffer.toString() == "." -> inputBuffer.clear()
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
                targetURIScheme = R.scheme;
                targetURIauthority = R.authority;
                targetURIpath = removeDotSegments(R.path);
                targetURIquery = R.query;
            } else {
                if (R.authority != null) {
                    targetURIauthority = R.authority
                    targetURIpath = removeDotSegments(R.path)
                    targetURIquery = R.query
                } else {
                    if (R.path.isEmpty()) {
                        targetURIpath = B.path;
                        targetURIquery = R.query ?: B.query;
                    } else {
                        targetURIpath = removeDotSegments(R.path.takeIf { R.path.startsWith("/") } ?: merge())
                        targetURIquery = R.query
                    }
                    targetURIauthority = B.authority
                }
                targetURIScheme = B.scheme;
            }

            val targetURIfragment: String? = R.fragment;

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

data class Collection(val list: List<RdfTripleElement>) : RdfTripleElement(), List<RdfTripleElement> by list

open class Literal(val literalValue: Any, val datatype: BaseDatatype) : RdfTripleElement() {

    //TODO(support: http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral + http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML
    // || see:https://www.w3.org/TR/rdf11-concepts/#datatype-iris)
    companion object {
        fun fromNonNumericLiteral(lexicalForm: String, datatypeIRI: IRI): Literal {
            val datatype = when {
                datatypeIRI.iri.startsWith(IRIConstants.XSD_IRI) -> when (datatypeIRI.fragment) {
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
            return Literal(lexicalForm.takeUnless { datatype is XSDDatatype } ?: datatype.parse(lexicalForm),
                datatype)
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