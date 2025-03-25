package entities.rdfsurfaces.rdf_term

import entities.rdfsurfaces.rdf_term.IRI.Companion.from
import util.IRIConstants

sealed class Literal : RdfTerm {
    abstract val lexicalValue: String
    abstract val literalValue: Any
    abstract val datatypeIRI: IRI

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is Literal -> other.lexicalValue == lexicalValue && other.literalValue == literalValue && other.datatypeIRI == datatypeIRI
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = literalValue.hashCode()
        result = 31 * result + datatypeIRI.hashCode()
        result = 31 * result + lexicalValue.hashCode()
        return result
    }
}

data class DefaultLiteral(
    override val lexicalValue: String,
    override val datatypeIRI: IRI,
    override val literalValue: Any
) : Literal()

data class LanguageTaggedString(
    override val lexicalValue: String,
    val langTag: String,
    val normalizedLangTag: String
) : Literal() {

    override val datatypeIRI: IRI = from(IRIConstants.RDF_LANG_STRING_IRI)
    override val literalValue
        get() = lexicalValue to normalizedLangTag

}