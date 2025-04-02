package app.interfaces.services.coder

import entities.rdfsurfaces.rdf_term.RDFTerm

interface N3SRDFTermCoderService {
    fun <T : RDFTerm> encode(rdfTerm: T): T
}