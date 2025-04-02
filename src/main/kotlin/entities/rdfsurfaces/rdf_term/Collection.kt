package entities.rdfsurfaces.rdf_term

data class Collection(val list: List<RDFTerm> = emptyList()) :
    RDFTerm,
    List<RDFTerm> by list
