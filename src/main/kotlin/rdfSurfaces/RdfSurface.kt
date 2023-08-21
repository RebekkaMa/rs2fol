package rdfSurfaces

abstract class RDFSurface(
    val graffiti: List<BlankNode>,
    val hayesGraph: List<HayesGraphElement>
) : HayesGraphElement()

//TODO(" check leanness ")
//TODO(" isomorphic check ")

class PositiveSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    fun getQuerySurfaces(): List<QuerySurface> = hayesGraph.filterIsInstance(QuerySurface::class.java)
    //.mapNotNull { if (it is QuerySurface) it else null}

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

    fun replaceBlankNodes(list: Set<List<RdfTripleElement>>): PositiveSurface {
        val maps = list.map {
            //TODO()
            if (graffiti.size != it.size) throw IllegalArgumentException()
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