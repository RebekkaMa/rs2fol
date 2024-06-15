package model.rdf_term

sealed interface Collection : RdfTerm {
    companion object {
        fun fromTerms(vararg terms: RdfTerm): Collection {
            return terms.foldRight<RdfTerm, Collection>(initial = CollectionEnd){ left, right ->
                CollectionPair(
                        left,
                        right
                )
            }
        }
    }
}

data class CollectionPair(val left: RdfTerm, val right: Collection) : Collection
data object CollectionEnd : Collection