package app.interfaces.services

import app.interfaces.results.RdfSurfaceParserResult
import entities.rdfsurfaces.rdf_term.IRI
import util.commandResult.Result
import util.commandResult.RootError

interface RDFSurfaceParseService {
    fun parseToEnd(input: String, baseIRI: IRI, useRDFLists: Boolean): Result<RdfSurfaceParserResult.Success.Parsed, RootError>
}