package entities.rdfsurfaces.rdf_term

import entities.rdfsurfaces.rdf_term.IRI.Companion.from
import org.apache.jena.datatypes.BaseDatatype.TypedValue
import org.apache.jena.graph.langtag.LangTags
import org.apache.jena.rdf.model.ModelFactory
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
) : Literal(), org.apache.jena.rdf.model.Literal by ModelFactory.createDefaultModel()
    .createTypedLiteral(lexicalValue, datatypeIRI.iri) {

    override val literalValue: Any = (value as? TypedValue)?.lexicalValue ?: value

    companion object {

        fun fromNumericLiteral(numericLiteral: String): Literal =
            when {
                numericLiteral.contains(
                    "E",
                    ignoreCase = true
                ) -> DefaultLiteral(
                    lexicalValue = numericLiteral,
                    datatypeIRI = from(IRIConstants.XSD_DOUBLE)
                )

                numericLiteral.contains(
                    ".",
                    ignoreCase = true
                ) -> DefaultLiteral(
                    lexicalValue = numericLiteral,
                    datatypeIRI = from(IRIConstants.XSD_DECIMAL)
                )

                else -> DefaultLiteral(
                    lexicalValue = numericLiteral,
                    datatypeIRI = from(IRIConstants.XSD_INTEGER)
                )
            }
    }
}

data class LanguageTaggedString(
    override val lexicalValue: String,
    val langTag: String
) : Literal() {

    val normalizedLangTag: String = LangTags.formatLangtag(langTag)

    override val datatypeIRI: IRI = from(IRIConstants.RDF_LANG_STRING_IRI)
    override val literalValue
        get() = lexicalValue to normalizedLangTag

}