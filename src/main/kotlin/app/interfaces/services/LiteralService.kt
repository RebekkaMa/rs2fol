package app.interfaces.services

import entities.rdfsurfaces.rdf_term.IRI
import entities.rdfsurfaces.rdf_term.Literal

interface LiteralService {
    fun createDefaultLiteral(lexicalValue: String, datatypeIRI: IRI): Literal
    fun createLanguageTaggedString(lexicalValue: String, langTag: String): Literal
    fun createGeneralizedLiteral(literal: Literal): Literal
}