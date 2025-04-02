package entities.rdfsurfaces

import entities.rdfsurfaces.rdf_term.RDFTerm

data class RDFTriple(
    val rdfSubject: RDFTerm,
    val rdfPredicate: RDFTerm,
    val rdfObject: RDFTerm
) : HayesGraphElement()