package model.rdf_term

data class Collection(val list: List<RdfTerm> = listOf()) :
    RdfTerm(),
    List<RdfTerm> by list
