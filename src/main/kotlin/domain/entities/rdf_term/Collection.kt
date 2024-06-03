package domain.entities.rdf_term

data class Collection(val list: List<RdfTerm> = emptyList()) :
    RdfTerm,
    List<RdfTerm> by list
