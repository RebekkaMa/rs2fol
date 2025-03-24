package interfaces

import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.rdf_term.IRI
import util.commandResult.Error
import util.commandResult.IntermediateStatus

interface RDFSurfaceParseService {
    fun parseToEnd(input: String, baseIRI: IRI): IntermediateStatus<PositiveSurface, Error>
}