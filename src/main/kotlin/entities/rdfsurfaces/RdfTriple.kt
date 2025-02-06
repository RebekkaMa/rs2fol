package entities.rdfsurfaces

import entities.rdfsurfaces.rdf_term.RdfTerm

data class RdfTriple(
    val rdfSubject: RdfTerm,
    val rdfPredicate: RdfTerm,
    val rdfObject: RdfTerm
) : HayesGraphElement()