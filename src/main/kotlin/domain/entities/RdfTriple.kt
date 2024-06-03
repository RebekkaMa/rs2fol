package domain.entities

import domain.entities.rdf_term.RdfTerm

data class RdfTriple(
    val rdfSubject: RdfTerm,
    val rdfPredicate: RdfTerm,
    val rdfObject: RdfTerm
) : HayesGraphElement()