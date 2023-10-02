package rdfSurfaces

sealed class RdfSurface(
    val graffiti: List<BlankNode>,
    val hayesGraph: List<HayesGraphElement>,
) : HayesGraphElement(), Cloneable {
    fun containsPositiveSurface() = hayesGraph.any { it is PositiveSurface }
    fun containsNegativeSurface() = hayesGraph.any { it is NegativeSurface }
    fun containsQuerySurface() = hayesGraph.any { it is QuerySurface }
    fun containsNeutralSurface() = hayesGraph.any { it is NeutralSurface }
    fun containsSurface() = hayesGraph.any { it is RdfSurface }

}

//TODO(" check leanness ")
//TODO(" isomorphic check ")

class PositiveSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RdfSurface(graffiti, hayesGraph) {
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
    RdfSurface(graffiti, hayesGraph) {
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
    RdfSurface(graffiti, hayesGraph) {

    private fun containsVariables(rdfSurface: RdfSurface = this): Boolean {
        if (rdfSurface.hayesGraph.isEmpty()) return false
        return rdfSurface.hayesGraph.any {
            when (it) {
                is RdfTriple -> (it.rdfSubject is BlankNode || it.rdfPredicate is BlankNode || it.rdfObject is BlankNode)
                is RdfSurface -> containsVariables(it)
            }
        }
    }

    private fun isBounded(blankNode: BlankNode, rdfSurface: RdfSurface = this) : Boolean {
        if (rdfSurface.hayesGraph.isEmpty()) return false
        return rdfSurface.hayesGraph.any{
            when (it) {
                is RdfTriple -> (it.rdfSubject == blankNode || it.rdfPredicate == blankNode || it.rdfObject == blankNode)
                is RdfSurface -> if (blankNode in it.graffiti) false else isBounded(blankNode, it)
            }
        }
    }

    fun replaceBlankNodes(list: Set<List<RdfTripleElement>>): PositiveSurface {

        fun replaceBlankNodes(map: Map<BlankNode, RdfTripleElement>, rdfSurface: RdfSurface): RdfSurface {

            val hayesGraph = rdfSurface.hayesGraph.map { hayesGraphElement ->
                return@map when (hayesGraphElement) {
                    is RdfTriple -> RdfTriple(
                        hayesGraphElement.rdfSubject.takeUnless { it is BlankNode }
                            ?: map[hayesGraphElement.rdfSubject] ?: hayesGraphElement.rdfSubject,
                        hayesGraphElement.rdfPredicate.takeUnless { it is BlankNode }
                            ?: map[hayesGraphElement.rdfPredicate] ?: hayesGraphElement.rdfPredicate,
                        hayesGraphElement.rdfObject.takeUnless { it is BlankNode }
                            ?: map[hayesGraphElement.rdfObject] ?: hayesGraphElement.rdfObject,
                    )

                    is RdfSurface -> replaceBlankNodes(map.minus(hayesGraphElement.graffiti.toSet()), hayesGraphElement)
                }
            }

            return when (rdfSurface) {
                is PositiveSurface -> PositiveSurface(rdfSurface.graffiti, hayesGraph)
                is NegativeSurface -> NegativeSurface(rdfSurface.graffiti, hayesGraph)
                is QuerySurface -> QuerySurface(rdfSurface.graffiti, hayesGraph)
                is NeutralSurface -> NeutralSurface(rdfSurface.graffiti, hayesGraph)
                is NegativeTripleSurface -> NegativeTripleSurface(rdfSurface.graffiti, hayesGraph)
            }
        }

        val maps = list.map {
            if (containsVariables().not()) return PositiveSurface(listOf(), this.hayesGraph)
            if (graffiti.size != it.size) {
                val relevantGraffiti = this.graffiti.filter { blankNode ->  isBounded(blankNode)}
                if (relevantGraffiti.size != it.size) throw IllegalArgumentException("The arity of the answer tuples doesn't match the number of graffiti on the query surface!")
                return@map buildMap {
                    relevantGraffiti.forEachIndexed { index, blankNode ->
                        this[blankNode] = it[index]
                    }
                }
            }

            buildMap {
                graffiti.forEachIndexed { index, blankNode ->
                    this[blankNode] = it[index]
                }
            }
        }
        return PositiveSurface(
            listOf(),
            maps.flatMap { map ->
                replaceBlankNodes(map, this).hayesGraph
            }.distinct()
        )
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
    RdfSurface(graffiti, hayesGraph) {
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
    RdfSurface(graffiti, hayesGraph) {
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