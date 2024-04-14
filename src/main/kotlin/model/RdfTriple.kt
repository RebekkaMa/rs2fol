package model

import model.rdf_term.RdfTerm

data class RdfTriple(
    val rdfSubject: RdfTerm,
    val rdfPredicate: RdfTerm,
    val rdfObject: RdfTerm ) : HayesGraphElement()