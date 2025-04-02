package app.interfaces.services

import app.interfaces.results.RDFSurfaceParserResult
import entities.rdfsurfaces.rdf_term.IRI
import util.commandResult.Result
import util.commandResult.RootError

interface RDFSurfaceParserService {
    fun parseToEnd(input: String, baseIRI: IRI, useRDFLists: Boolean): Result<RDFSurfaceParserResult.Success.Parsed, RootError>
}