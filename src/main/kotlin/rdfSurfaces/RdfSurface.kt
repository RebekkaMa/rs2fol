package rdfSurfaces

sealed class RdfSurface : HayesGraphElement() {

    abstract val graffiti: List<BlankNode>
    abstract val hayesGraph: List<HayesGraphElement>

    fun containsVariables(rdfSurface: RdfSurface = this): Boolean {
        if (rdfSurface.hayesGraph.isEmpty()) return false
        return rdfSurface.hayesGraph.any {
            when (it) {
                is RdfTriple -> (it.rdfSubject is BlankNode || it.rdfPredicate is BlankNode || it.rdfObject is BlankNode)
                is RdfSurface -> containsVariables(it)
            }
        }
    }

    fun isBounded(blankNode: BlankNode, rdfSurface: RdfSurface = this): Boolean {
        if (rdfSurface.hayesGraph.isEmpty()) return false
        return rdfSurface.hayesGraph.any {
            when (it) {
                is RdfTriple -> (it.rdfSubject == blankNode || it.rdfPredicate == blankNode || it.rdfObject == blankNode)
                is RdfSurface -> if (blankNode in it.graffiti) false else isBounded(blankNode, it)
            }
        }
    }
}

sealed class QSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface() {
    fun replaceBlankNodes(map: Map<BlankNode, RdfTripleElement>, rdfSurface: RdfSurface): RdfSurface {

        fun replaceBlankNodes(collection: Collection, map: Map<BlankNode, RdfTripleElement>): Collection {
            return Collection(collection.map {
                when (it) {
                    is BlankNode -> map[it] ?: it
                    is Collection -> replaceBlankNodes(it, map)
                    else -> it
                }
            })
        }

        val hayesGraph = rdfSurface.hayesGraph.map { hayesGraphElement ->
            return@map when (hayesGraphElement) {
                is RdfTriple -> RdfTriple(
                    when (hayesGraphElement.rdfSubject) {
                        is BlankNode -> map[hayesGraphElement.rdfSubject] ?: hayesGraphElement.rdfSubject
                        is Collection -> replaceBlankNodes(hayesGraphElement.rdfSubject, map)
                        else -> hayesGraphElement.rdfSubject
                    },
                    when (hayesGraphElement.rdfPredicate) {
                        is BlankNode -> map[hayesGraphElement.rdfPredicate] ?: hayesGraphElement.rdfPredicate
                        is Collection -> replaceBlankNodes(hayesGraphElement.rdfPredicate, map)
                        else -> hayesGraphElement.rdfPredicate
                    },
                    when (hayesGraphElement.rdfObject) {
                        is BlankNode -> map[hayesGraphElement.rdfObject] ?: hayesGraphElement.rdfObject
                        is Collection -> replaceBlankNodes(hayesGraphElement.rdfObject, map)
                        else -> hayesGraphElement.rdfObject
                    })

                is RdfSurface -> replaceBlankNodes(map.minus(hayesGraphElement.graffiti.toSet()), hayesGraphElement)
            }
        }

        return when (rdfSurface) {
            is PositiveSurface -> PositiveSurface(rdfSurface.graffiti, hayesGraph)
            is NegativeSurface -> NegativeSurface(rdfSurface.graffiti, hayesGraph)
            is QuerySurface -> QuerySurface(rdfSurface.graffiti, hayesGraph)
            is NeutralSurface -> NeutralSurface(rdfSurface.graffiti, hayesGraph)
            is NegativeTripleSurface -> NegativeTripleSurface(rdfSurface.graffiti, hayesGraph)
            is QuestionSurface -> QuestionSurface(rdfSurface.graffiti, hayesGraph)
            is AnswerSurface -> AnswerSurface(rdfSurface.graffiti, hayesGraph)
        }
    }

    abstract fun replaceBlankNodes(list: Set<List<RdfTripleElement>>): PositiveSurface

}

data class PositiveSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface() {


    fun getQSurfaces(): List<QSurface> = hayesGraph.filterIsInstance<QSurface>()

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

data class NegativeSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface() {
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

data class QuerySurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    QSurface(graffiti, hayesGraph) {

    override fun replaceBlankNodes(list: Set<List<RdfTripleElement>>): PositiveSurface {

        val maps = list.map {
            if (containsVariables().not()) return PositiveSurface(listOf(), this.hayesGraph)
            if (graffiti.size != it.size) {
                val relevantGraffiti = this.graffiti.filter { blankNode -> isBounded(blankNode) }
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

data class NeutralSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface() {
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

data class NegativeTripleSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface() {
    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is NegativeTripleSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

data class QuestionSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    QSurface(graffiti, hayesGraph) {

    override fun replaceBlankNodes(list: Set<List<RdfTripleElement>>): PositiveSurface {
        val maps = list.map {
            if (it.isEmpty() || this.containsVariables().not()) return PositiveSurface(
                listOf(),
                this.hayesGraph.filterIsInstance<AnswerSurface>().singleOrNull()?.hayesGraph
                    ?: throw IllegalArgumentException("A question surface must contain exactly one answer surface!")
            )
            if (graffiti.size != it.size) {
                val relevantGraffiti = this.graffiti.filter { blankNode -> isBounded(blankNode) }
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
        val answerSurface = this.hayesGraph.filterIsInstance<AnswerSurface>().singleOrNull()
            ?: throw IllegalArgumentException("A question surface must contain exactly one answer surface!")
        return PositiveSurface(
            listOf(),
            maps.flatMap { map ->
                replaceBlankNodes(map, answerSurface).hayesGraph
            }.distinct()
        )
    }


    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is QuestionSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}

data class AnswerSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface() {

    override fun equals(other: Any?): Boolean {
        return when {
            other === this -> true
            other is AnswerSurface -> other.graffiti == graffiti && other.hayesGraph == hayesGraph
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = graffiti.hashCode()
        result = 31 * result + hayesGraph.hashCode()
        return result
    }
}