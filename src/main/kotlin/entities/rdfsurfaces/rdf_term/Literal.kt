package entities.rdfsurfaces.rdf_term

import entities.rdfsurfaces.rdf_term.IRI.Companion.from
import util.IRIConstants

sealed class Literal : RdfTerm {
    abstract val lexicalValue: String
    abstract val datatypeIRI: IRI

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is Literal -> other.lexicalValue == lexicalValue && other.datatypeIRI == datatypeIRI
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = lexicalValue.hashCode()
        result = 31 * result + datatypeIRI.hashCode()
        return result
    }
}

data class DefaultLiteral(
    override val lexicalValue: String,
    override val datatypeIRI: IRI,
) : Literal()

data class LanguageTaggedString(
    override val lexicalValue: String,
    val langTag: String,
) : Literal() {

    override val datatypeIRI: IRI = from(IRIConstants.RDF_LANG_STRING_IRI)
}