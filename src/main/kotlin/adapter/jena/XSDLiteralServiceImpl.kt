package adapter.jena

import app.interfaces.results.XSDLiteralServiceResult
import app.interfaces.services.jena.XSDLiteralService
import entities.rdfsurfaces.rdf_term.DefaultLiteral
import entities.rdfsurfaces.rdf_term.IRI.Companion.from
import entities.rdfsurfaces.rdf_term.LanguageTaggedString
import entities.rdfsurfaces.rdf_term.Literal
import org.apache.jena.datatypes.BaseDatatype
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.datatypes.xsd.impl.*
import org.apache.jena.graph.langtag.LangTags
import org.apache.jena.rdf.model.ModelFactory
import util.commandResult.RootError
import util.commandResult.success

class XSDLiteralServiceImpl : XSDLiteralService {
    private val model = ModelFactory.createDefaultModel()

    override fun createGeneralizedLiteral(literal: Literal): util.commandResult.Result<XSDLiteralServiceResult.Success.Literal, RootError> {
        try {
            val jenaLiteral = model.createTypedLiteral(literal.lexicalValue, literal.datatypeIRI.iri)
            val generalizedValue = (jenaLiteral.value as? BaseDatatype.TypedValue)?.lexicalValue ?: jenaLiteral.value

            if (literal is LanguageTaggedString) {
                return success(
                    XSDLiteralServiceResult.Success.Literal(
                        literal = LanguageTaggedString(
                            lexicalValue = generalizedValue.toString(),
                            langTag = LangTags.formatLangtag(literal.langTag),
                        )
                    )
                )
            }

            val lit = when (jenaLiteral.datatype) {
                is XSDBaseNumericType -> {
                    DefaultLiteral(
                        lexicalValue = generalizedValue.toString(),
                        datatypeIRI = from(XSDDatatype.XSDdecimal.uri),
                    )
                }

                is XSDBaseStringType -> {
                    DefaultLiteral(
                        lexicalValue = generalizedValue.toString(),
                        datatypeIRI = from(XSDDatatype.XSDstring.uri),
                    )
                }

                is XSDDurationType, is XSDDayTimeDurationType, is XSDYearMonthDurationType -> {
                    DefaultLiteral(
                        lexicalValue = literal.lexicalValue,
                        datatypeIRI = from(XSDDatatype.XSDduration.uri),
                    )
                }

                is XSDDateTimeType -> {
                    DefaultLiteral(
                        lexicalValue = generalizedValue.toString(),
                        datatypeIRI = from(XSDDatatype.XSDdateTime.uri),
                    )
                }

                else -> literal
            }
            return success(
                XSDLiteralServiceResult.Success.Literal(lit)
            )
        } catch (e: Exception) {
            return util.commandResult.error(
                XSDLiteralServiceResult.Error.InvalidLexicalValue(
                    literal.lexicalValue,
                    literal.datatypeIRI.iri
                )
            )
        }
    }
}