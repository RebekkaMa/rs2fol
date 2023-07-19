package rdfSurfaces

abstract class RDFSurface(
    val graffiti: List<BlankNode>,
    val hayesGraph: List<HayesGraphElement>
) : HayesGraphElement()

class PositiveRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph) {
    fun getQuerySurfaces(): List<QueryRDFSurface> = hayesGraph.filterIsInstance(QueryRDFSurface::class.java)
    //.mapNotNull { if (it is QueryRDFSurface) it else null}

}

class NegativeRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph)

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

                        else -> throw Exception("Not supported yet")
                    }
                }
            }
        )
    }
}

class NeutralRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph)

class NegativeTripleRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph)