package rdfSurfaces

import rdfSurfaces.rdfTerm.RdfTerm

data class RdfTriple(
    val rdfSubject: RdfTerm,
    val rdfPredicate: RdfTerm,
    val rdfObject: RdfTerm ) : HayesGraphElement()