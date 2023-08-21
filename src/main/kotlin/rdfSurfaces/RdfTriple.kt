package rdfSurfaces

data class RdfTriple(
    val rdfSubject: RdfTripleElement,
    val rdfPredicate: RdfTripleElement,
    val rdfObject: RdfTripleElement, ) : HayesGraphElement()