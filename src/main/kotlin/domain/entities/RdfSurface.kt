package domain.entities

import domain.entities.rdf_term.BlankNode
import domain.entities.rdf_term.RdfTerm
import domain.entities.rdf_term.Collection


sealed class RdfSurface : HayesGraphElement() {

    abstract val graffiti: List<BlankNode>
    abstract val hayesGraph: List<HayesGraphElement>

    fun containsVariables(): Boolean {
        if (this.hayesGraph.isEmpty()) return false
        return this.hayesGraph.any {
            when (it) {
                is RdfTriple -> (it.rdfSubject is BlankNode || it.rdfPredicate is BlankNode || it.rdfObject is BlankNode)
                is RdfSurface -> it.containsVariables()
            }
        }
    }

    fun isBounded(blankNode: BlankNode): Boolean {
        if (hayesGraph.isEmpty()) return false
        return hayesGraph.any { child ->
            when (child) {
                is RdfTriple -> (child.rdfSubject == blankNode || child.rdfPredicate == blankNode || child.rdfObject == blankNode)
                is RdfSurface -> if (blankNode in child.graffiti) false else child.isBounded(blankNode = blankNode)
            }
        }
    }
}

sealed class QSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface() {
    fun replaceBlankNodes(map: Map<BlankNode, RdfTerm>, rdfSurface: RdfSurface): RdfSurface {

        fun replaceBlankNodes(collection: Collection, map: Map<BlankNode, RdfTerm>): Collection {
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
            is NegativeComponentSurface -> NegativeComponentSurface(rdfSurface.graffiti, hayesGraph)
            is NegativeAnswerSurface -> NegativeAnswerSurface(rdfSurface.graffiti, hayesGraph)
        }
    }

    abstract fun replaceBlankNodes(list: Set<List<RdfTerm>>): PositiveSurface

}

data class PositiveSurface(override val graffiti: List<BlankNode> = emptyList(), override val hayesGraph: List<HayesGraphElement> = emptyList()) :
    RdfSurface() {

    fun getQSurfaces(): List<QSurface> = hayesGraph.filterIsInstance<QSurface>()

}

data class NegativeSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface()

data class QuerySurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    QSurface(graffiti, hayesGraph) {

    override fun replaceBlankNodes(list: Set<List<RdfTerm>>): PositiveSurface {

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
}

data class NeutralSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface()

data class NegativeTripleSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface()

data class QuestionSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    QSurface(graffiti, hayesGraph) {

    override fun replaceBlankNodes(list: Set<List<RdfTerm>>): PositiveSurface {
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
}

data class AnswerSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface()

data class NegativeAnswerSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface()

data class NegativeComponentSurface(override val graffiti: List<BlankNode>, override val hayesGraph: List<HayesGraphElement>) :
    RdfSurface()