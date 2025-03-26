package adapter.jena

import app.interfaces.services.LiteralService
import entities.rdfsurfaces.rdf_term.DefaultLiteral
import entities.rdfsurfaces.rdf_term.IRI
import entities.rdfsurfaces.rdf_term.IRI.Companion.from
import entities.rdfsurfaces.rdf_term.LanguageTaggedString
import entities.rdfsurfaces.rdf_term.Literal
import org.apache.jena.datatypes.BaseDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType
import org.apache.jena.graph.langtag.LangTags
import org.apache.jena.rdf.model.ModelFactory

class LiteralServiceImpl : LiteralService {
    private val model = ModelFactory.createDefaultModel()

    override fun createDefaultLiteral(lexicalValue: String, datatypeIRI: IRI): Literal {
        val jenaLiteral = model.createTypedLiteral(lexicalValue, datatypeIRI.iri)
        return DefaultLiteral(
            lexicalValue = lexicalValue,
            datatypeIRI = from(datatypeIRI.iri) ,
            literalValue = (jenaLiteral.value as? BaseDatatype.TypedValue)?.lexicalValue ?: jenaLiteral.value
        )
    }

    override fun createLanguageTaggedString(lexicalValue: String, langTag: String): LanguageTaggedString {
        return LanguageTaggedString(
            lexicalValue = lexicalValue,
            langTag = langTag,
            normalizedLangTag = LangTags.formatLangtag(langTag),
        )
    }

    override fun createGeneralizedLiteral(literal: Literal): Literal {
        val jenaLiteral = model.createTypedLiteral(literal.lexicalValue, literal.datatypeIRI.iri)

        return when (jenaLiteral.datatype) {
            is XSDBaseNumericType -> {
                DefaultLiteral(
                    lexicalValue = literal.lexicalValue,
                    datatypeIRI = from(XSDDatatype.XSDdecimal.uri) ,
                    literalValue = (jenaLiteral.value as? BaseDatatype.TypedValue)?.lexicalValue ?: jenaLiteral.value
                )
            }
            is XSDBaseStringType -> {
                DefaultLiteral(
                    lexicalValue = literal.lexicalValue,
                    datatypeIRI = from(XSDDatatype.XSDstring.uri) ,
                    literalValue = (jenaLiteral.value as? BaseDatatype.TypedValue)?.lexicalValue ?: jenaLiteral.value
                )
            }
            else -> literal
        }
    }
}