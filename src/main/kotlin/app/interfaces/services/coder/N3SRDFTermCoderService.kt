package app.interfaces.services.coder

import entities.rdfsurfaces.rdf_term.RdfTerm

interface N3SRDFTermCoderService {
    fun <T : RdfTerm> encode(rdfTerm: T): T
}