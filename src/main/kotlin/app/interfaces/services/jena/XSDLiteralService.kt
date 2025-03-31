package app.interfaces.services.jena

import app.interfaces.results.XSDLiteralServiceResult
import entities.rdfsurfaces.rdf_term.Literal
import util.commandResult.Result
import util.commandResult.RootError


interface XSDLiteralService {
    fun createGeneralizedLiteral(literal: Literal): Result<XSDLiteralServiceResult.Success.Literal, RootError>
}