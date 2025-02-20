package use_cases.modelTransformer

import entities.fol.*
import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import entities.rdfsurfaces.*
import entities.rdfsurfaces.rdf_term.*
import entities.rdfsurfaces.rdf_term.Collection
import util.SurfaceNotSupportedException
import util.commandResult.Error
import util.commandResult.IntermediateStatus

object RdfSurfaceModelToTPTPModelUseCase {
    operator fun invoke(
        defaultPositiveSurface: PositiveSurface,
        ignoreQuerySurfaces: Boolean = false,
        tptpName: String = "axiom",
        formulaRole: FormulaRole = FormulaRole.Axiom,
        dEntailment: Boolean = false,
    ): IntermediateStatus<List<AnnotatedFormula>, SurfaceNotSupportedError> {

        fun transform(blankNode: BlankNode) = FOLVariable(blankNode.blankNodeId)

        fun transform(iri: IRI) = FOLConstant(iri.iri)

        fun transform(literal: Literal) = FOLConstant(
            when (literal) {
                is LanguageTaggedString -> {
                    if (dEntailment) "\"${literal.lexicalValue}\"@${literal.normalizedLangTag}"
                    else "\"${literal.lexicalValue}\"@${literal.langTag}"
                }

                else -> {
                    if (dEntailment) "\"${literal.literalValue}\"^^${literal.datatypeIRI.iri}"
                    else "\"${literal.lexicalValue}\"^^${literal.datatypeIRI.iri}"
                }
            }
        )


        fun transform(rdfTerm: RdfTerm): FOLExpression = when (rdfTerm) {
            is BlankNode -> transform(rdfTerm)
            is Literal -> transform(rdfTerm)
            is IRI -> transform(rdfTerm)
            is Collection -> FOLPredicate("list", rdfTerm.list.map { transform(it) })
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

                        is NegativeSurface, is QuerySurface, is NegativeTripleSurface, is NegativeAnswerSurface, is NegativeComponentSurface -> {
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

                        is NeutralSurface, is QuestionSurface, is AnswerSurface -> throw SurfaceNotSupportedException(
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

        val (querySurfaces, questionSurface, otherHayesGraphElements) = defaultPositiveSurface.hayesGraph.fold(
            Triple<List<QuerySurface>, List<QuestionSurface>, List<HayesGraphElement>>(
                emptyList(), emptyList(), emptyList()
            )
        ) { acc, hayesGraphElement ->
            when (hayesGraphElement) {
                is QuerySurface -> acc.copy(first = acc.first.plus(hayesGraphElement))
                is QuestionSurface -> acc.copy(second = acc.second.plus(hayesGraphElement))
                else -> acc.copy(third = acc.third.plus(hayesGraphElement))
            }
        }

        return try {

            val fofQuantifiedFormula = otherHayesGraphElements.let {
                val fofFormula = run {
                    val expression =
                        when {
                            it.isEmpty() -> return@run FOLTrue
                            otherHayesGraphElements.size == 1 -> transform(otherHayesGraphElements.single())
                            else -> FOLAnd(otherHayesGraphElements.map { transform(it) })
                        }
                    if (fofVariableList.isEmpty()) return@run expression
                    FOLExists(
                        variables = fofVariableList,
                        expression = expression
                    )
                }
                AnnotatedFormula(
                    name = tptpName,
                    type = formulaRole.toFormulaType(),
                    expression = fofFormula
                )
            }

            val fofQuantifiedFormulaQuery = querySurfaces.mapIndexed { index, surface ->
                val folFormula = run {
                    val expression =
                        when {
                            surface.hayesGraph.isEmpty() -> return@run FOLTrue
                            surface.hayesGraph.size == 1 -> transform(surface.hayesGraph.single())
                            else -> FOLAnd(surface.hayesGraph.map { transform(it) })
                        }
                    if (surface.graffiti.isEmpty()) return@run expression
                    FOLExists(
                        variables = transform(surface.graffiti),
                        expression = expression
                    )

                }

                AnnotatedFormula(
                    name = "query_$index",
                    type = FormulaType.Question,
                    expression = folFormula
                )
            }

            val fofQuantifiedFormulaQuestion = questionSurface.mapIndexed { index, surface ->
                val hayesGraphElementsWithoutAnswerSurface = surface.hayesGraph.filterNot { it is AnswerSurface }
                val fofFormula = kotlin.run {
                    val expression =
                        when {
                            hayesGraphElementsWithoutAnswerSurface.isEmpty() -> return@run FOLTrue
                            hayesGraphElementsWithoutAnswerSurface.size == 1 -> transform(
                                hayesGraphElementsWithoutAnswerSurface.single()
                            )

                            else -> FOLAnd(hayesGraphElementsWithoutAnswerSurface.map { transform(it) })
                        }
                    if (surface.graffiti.isEmpty()) return@run expression
                    FOLExists(
                        variables = transform(surface.graffiti),
                        expression = expression
                    )
                }
                AnnotatedFormula(
                    name = "question_$index",
                    type = FormulaType.Question,
                    expression = fofFormula
                )
            }

            IntermediateStatus.Result(
                buildList {
                    add(fofQuantifiedFormula)
                    if (ignoreQuerySurfaces) return@buildList
                    addAll(fofQuantifiedFormulaQuery)
                    addAll(fofQuantifiedFormulaQuestion)
                }
            )
        } catch (exception: SurfaceNotSupportedException) {
            return IntermediateStatus.Error(SurfaceNotSupportedError(surface = exception.surface))
        }
    }

}

enum class FormulaRole {
    Axiom,
    Conjecture,
    Hypothesis,
    Lemma,
    Question
}

private fun FormulaRole.toFormulaType() = when (this) {
    FormulaRole.Axiom -> FormulaType.Axiom
    FormulaRole.Conjecture -> FormulaType.Conjecture
    FormulaRole.Hypothesis -> FormulaType.Hypothesis
    FormulaRole.Lemma -> FormulaType.Lemma
    FormulaRole.Question -> FormulaType.Question
}

data class SurfaceNotSupportedError(val surface: String) : Error