package rdfSurfaces

abstract class RDFSurface(
    val graffiti: List<BlankNode>,
    val hayesGraph: List<HayesGraphElement>
) : HayesGraphElement()

//TODO(" check leanness ")
//TODO(" isomorphic check ")

class PositiveRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    fun getQuerySurfaces(): List<QueryRDFSurface> = hayesGraph.filterIsInstance(QueryRDFSurface::class.java)
    //.mapNotNull { if (it is QueryRDFSurface) it else null}

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is PositiveRDFSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

class NegativeRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is NegativeRDFSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

class QueryRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {

    fun replaceBlankNodes(list: Set<List<RdfTripleElement>>): PositiveRDFSurface {
        val maps = list.map {
            //TODO()
            if (graffiti.size != it.size) throw IllegalArgumentException()
            buildMap {
                graffiti.forEachIndexed { index, blankNode ->
                    this[blankNode] = it[index]
                }
            }
        }
        return PositiveRDFSurface(
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
            other is QueryRDFSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

class NeutralRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is NeutralRDFSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

class NegativeTripleRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is NegativeRDFSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}