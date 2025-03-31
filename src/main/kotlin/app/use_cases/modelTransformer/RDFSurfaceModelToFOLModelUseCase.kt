package app.use_cases.modelTransformer

import app.use_cases.results.modelTransformerResults.RDFSurfaceModelToFOLModelResult
import entities.fol.*
import entities.rdfsurfaces.*
import entities.rdfsurfaces.rdf_term.*
import entities.rdfsurfaces.rdf_term.Collection
import util.IRIConstants
import util.SurfaceNotSupportedException
import util.commandResult.Result
import util.commandResult.success

class RDFSurfaceModelToFOLModelUseCase {
    operator fun invoke(
        defaultPositiveSurface: PositiveSurface,
        listType: ListType = ListType.FUNCTION
    ): Result<RDFSurfaceModelToFOLModelResult.Success.Formulas, RDFSurfaceModelToFOLModelResult.Error> {

        fun transform(blankNode: BlankNode) = FOLVariable(blankNode.blankNodeId)

        fun transform(iri: IRI) = FOLConstant(iri.iri)

        fun transform(literal: Literal) = FOLConstant(
            when (literal) {
                is LanguageTaggedString -> {
                    "\"${literal.lexicalValue}\"@${literal.langTag}"
                }

                else -> {
                    "\"${literal.lexicalValue}\"^^${literal.datatypeIRI.iri}"
                }
            }
        )


        fun transform(rdfTerm: RdfTerm): GeneralTerm {
            return when (rdfTerm) {
                is BlankNode -> transform(rdfTerm)
                is Literal -> transform(rdfTerm)
                is IRI -> transform(rdfTerm)
                is Collection -> {
                    return when (listType) {
                        ListType.NESTED_FUNCTIONS -> {
                            if (rdfTerm.size == 0) return transform(IRI.from(IRIConstants.RDF_NIL_IRI))
                            FOLFunction(
                                "list",
                                listOf(
                                    transform(rdfTerm.list.first()),
                                    transform(Collection(rdfTerm.list.drop(1)))
                                )
                            )
                        }

                        ListType.FUNCTION -> FOLFunction(
                            "list",
                            rdfTerm.list.map { transform(it) }
                        )
                    }
                }
            }
        }

        fun transform(blankNodeList: List<BlankNode>) = blankNodeList.map { transform(it) }

        fun transform(hayesGraphElement: HayesGraphElement): FOLExpression {

            return when (hayesGraphElement) {
                is RdfSurface -> {
                    val fofVariableList = transform(hayesGraphElement.graffiti)
                    when (hayesGraphElement) {
                        is PositiveSurface -> {
                            val expression =
                                when {
                                    hayesGraphElement.hayesGraph.isEmpty() -> FOLTrue
                                    hayesGraphElement.hayesGraph.size == 1 -> transform(hayesGraphElement.hayesGraph.single())
                                    else -> FOLAnd(hayesGraphElement.hayesGraph.map { transform(it) })
                                }
                            if (fofVariableList.isEmpty()) return expression
                            FOLExists(
                                variables = fofVariableList,
                                expression = expression
                            )
                        }

                        is NegativeSurface, is QuerySurface, is NegativeAnswerSurface -> {
                            val expression =
                                when {
                                    hayesGraphElement.hayesGraph.isEmpty() -> FOLTrue
                                    hayesGraphElement.hayesGraph.size == 1 -> transform(hayesGraphElement.hayesGraph.single())
                                    else -> FOLAnd(hayesGraphElement.hayesGraph.map { transform(it) })
                                }

                            val negExpression = FOLNot(expression)
                            if (fofVariableList.isEmpty()) return negExpression
                            FOLForAll(
                                variables = fofVariableList,
                                expression = negExpression
                            )
                        }

                        is NeutralSurface -> throw SurfaceNotSupportedException(
                            hayesGraphElement.javaClass.name
                        )

                    }
                }

                is RdfTriple -> FOLPredicate(
                    name = "triple",
                    arguments = listOf(
                        transform(hayesGraphElement.rdfSubject),
                        transform(hayesGraphElement.rdfPredicate),
                        transform(hayesGraphElement.rdfObject)
                    )
                )
            }
        }

        val fofVariableList = transform(defaultPositiveSurface.graffiti)

        val (qSurfaces, otherHayesGraphElements) = defaultPositiveSurface.hayesGraph.fold(
            Pair<List<QSurface>, List<HayesGraphElement>>(
                emptyList(), emptyList()
            )
        ) { acc, hayesGraphElement ->
            when (hayesGraphElement) {
                is QSurface -> acc.copy(first = acc.first.plus(hayesGraphElement))
                else -> acc.copy(second = acc.second.plus(hayesGraphElement))
            }
        }

        return try {
            val fofQuantifiedFormula = otherHayesGraphElements.let {
                    val expression =
                        when {
                            it.isEmpty() -> return@let FOLTrue
                            otherHayesGraphElements.size == 1 -> transform(otherHayesGraphElements.single())
                            else -> FOLAnd(otherHayesGraphElements.map { transform(it) })
                        }
                    if (fofVariableList.isEmpty()) return@let expression
                    FOLExists(
                        variables = fofVariableList,
                        expression = expression
                    )
            }

            val fofQuantifiedFormulaQuery = qSurfaces.mapIndexed { index, surface ->
                    val expression =
                        when {
                            surface.hayesGraph.isEmpty() -> return@mapIndexed FOLTrue
                            surface.hayesGraph.size == 1 -> transform(surface.hayesGraph.single())
                            else -> FOLAnd(surface.hayesGraph.map { transform(it) })
                        }
                    if (surface.graffiti.isEmpty()) return@mapIndexed expression
                    FOLExists(
                        variables = transform(surface.graffiti),
                        expression = expression
                    )
                }

            success(
                RDFSurfaceModelToFOLModelResult.Success.Formulas(
                    formula = fofQuantifiedFormula,
                    queryFormulas = fofQuantifiedFormulaQuery
                )
            )

        } catch (exception: SurfaceNotSupportedException) {
            return Result.Error(RDFSurfaceModelToFOLModelResult.Error.SurfaceNotSupported(surface = exception.surface))
        }
    }

}

enum class ListType {
    NESTED_FUNCTIONS,
    FUNCTION
}
