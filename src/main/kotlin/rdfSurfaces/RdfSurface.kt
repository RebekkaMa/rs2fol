package rdfSurfaces

abstract class RDFSurface(
    val graffiti: List<BlankNode>,
    val hayesGraph: List<HayesGraphElement>,
) : HayesGraphElement() {
    fun containsPositiveSurface() = hayesGraph.any { it is PositiveSurface }
    fun containsNegativeSurface() = hayesGraph.any { it is NegativeSurface }
    fun containsQuerySurface() = hayesGraph.any { it is QuerySurface }
    fun containsNeutralSurface() = hayesGraph.any { it is NeutralSurface }
    fun containsSurface() = hayesGraph.any { it is RDFSurface }

}

//TODO(" check leanness ")
//TODO(" isomorphic check ")

class PositiveSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    fun getQuerySurfaces(): List<QuerySurface> = hayesGraph.filterIsInstance(QuerySurface::class.java)

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is PositiveSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

class NegativeSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is NegativeSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

class QuerySurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {

    fun containsVariables(rdfSurface: RDFSurface = this): Boolean {
        if (rdfSurface.hayesGraph.isEmpty()) return false
        return rdfSurface.hayesGraph.any {
            when (it) {
                is RdfTriple -> (it.rdfSubject is BlankNode || it.rdfPredicate is BlankNode || it.rdfObject is BlankNode)
                is RDFSurface -> containsVariables(it)
            }
        }
    }

    fun replaceBlankNodes(list: Set<List<RdfTripleElement>>): PositiveSurface {
        val maps = list.map {
            if (containsVariables().not()) return PositiveSurface(listOf(), this.hayesGraph)
            if (graffiti.size != it.size) throw IllegalArgumentException()

            //TODO()

            buildMap {
                graffiti.forEachIndexed { index, blankNode ->
                    this[blankNode] = it[index]
                }
            }
        }
        return PositiveSurface(
            listOf(),
            maps.flatMap { map ->
                hayesGraph.map { hayesGraphElement ->
                    when (hayesGraphElement) {
                        is RdfTriple -> return@map RdfTriple(
                            hayesGraphElement.rdfSubject.takeUnless { it is BlankNode }
                                ?: map[hayesGraphElement.rdfSubject] ?: hayesGraphElement.rdfSubject,
                            hayesGraphElement.rdfPredicate.takeUnless { it is BlankNode }
                                ?: map[hayesGraphElement.rdfPredicate] ?: hayesGraphElement.rdfPredicate,
                            hayesGraphElement.rdfObject.takeUnless { it is BlankNode }
                                ?: map[hayesGraphElement.rdfObject] ?: hayesGraphElement.rdfObject,
                        )

                        else -> throw Exception("Nested Surfaces on the query surface are not supported yet")
                    }
                }
            }
        )
    }

    private fun dfd() {

    }

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is QuerySurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

class NeutralSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is NeutralSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

class NegativeTripleSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is NegativeSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}