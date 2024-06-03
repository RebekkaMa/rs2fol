package domain.use_cases.transform

import domain.entities.*
import domain.entities.rdf_term.*
import domain.entities.rdf_term.Collection
import domain.error.Error
import domain.error.Result
import interface_adapters.services.transforming.TptpElementCoderService
import util.SurfaceNotSupportedException

object RdfSurfaceModelToFolUseCase {

    operator fun invoke(
        defaultPositiveSurface: PositiveSurface,
        ignoreQuerySurfaces: Boolean = false,
        tptpName: String = "axiom",
        formulaRole: String = "axiom",
    ): Result<String, SurfaceNotSupportedError> {

        val spaceBase = "   "
        val doubleSpaceBase = spaceBase.repeat(2)

        fun transform(blankNode: BlankNode) = TptpElementCoderService.encodeToValidTPTPVariable(blankNode.blankNodeId)

        fun transform(iri: IRI) = "'${TptpElementCoderService.encodeToValidTPTPLiteral(iri.iri)}'"

        fun transform(literal: Literal) = "'" + TptpElementCoderService.encodeToValidTPTPLiteral(
            when (literal) {
                is LanguageTaggedString -> "\"${literal.lexicalForm}\"@${literal.languageTag}"
                else -> "\"${literal.literalValue}\"^^${literal.datatype.uri}"
            }
        ) + "'"

        fun transform(rdfTerm: RdfTerm): String = when (rdfTerm) {
            is BlankNode -> transform(rdfTerm)
            is Literal -> transform(rdfTerm)
            is IRI -> transform(rdfTerm)
            is Collection -> "list".takeIf { rdfTerm.list.isEmpty() }
                ?: ("list(" + rdfTerm.list.joinToString(",") { transform(it) } + ")")
        }

        fun transform(blankNodeList: List<BlankNode>) = blankNodeList.joinToString(separator = ",") { transform(it) }

        fun transform(hayesGraphElement: HayesGraphElement, depth: Int): String {
            val newDepthSpace = spaceBase.repeat(depth.takeIf { it >= 0 } ?: 0)
            val nextDepthSpace = newDepthSpace.plus(spaceBase)

            return when (hayesGraphElement) {
                is RdfSurface -> {
                    val fofVariableList = transform(hayesGraphElement.graffiti)
                    when (hayesGraphElement) {
                        is PositiveSurface -> if (hayesGraphElement.graffiti.isEmpty()) {
                            if (hayesGraphElement.hayesGraph.isEmpty()) "\$true" else
                                hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "",
                                    separator = "${System.lineSeparator()}$newDepthSpace& ",
                                    postfix = ""
                                ) { transform(hayesGraphElement = it, depth =  depth + 1) }
                        } else {
                            if (hayesGraphElement.hayesGraph.isEmpty()) "? [$fofVariableList] : (${System.lineSeparator()}$nextDepthSpace\$true${System.lineSeparator()}$newDepthSpace)" else
                                hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "? [$fofVariableList] : (${System.lineSeparator()}$nextDepthSpace",
                                    separator = "${System.lineSeparator()}$nextDepthSpace& ",
                                    postfix = "${System.lineSeparator()}$newDepthSpace)"
                                ) { transform(it, depth + 1) }
                        }


                        is NegativeSurface, is QuerySurface, is NegativeTripleSurface -> if (hayesGraphElement.graffiti.isEmpty()) {
                            if (hayesGraphElement.hayesGraph.isEmpty()) "\$false" else
                                hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "~(${System.lineSeparator()}$nextDepthSpace",
                                    separator = "${System.lineSeparator()}$nextDepthSpace& ",
                                    postfix = "${System.lineSeparator()}$newDepthSpace)"
                                ) { transform(it, depth + 1) }
                        } else {
                            if (hayesGraphElement.hayesGraph.isEmpty()) "! [$fofVariableList] : ~(${System.lineSeparator()}$nextDepthSpace\$false${System.lineSeparator()}$newDepthSpace)" else
                                hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "! [$fofVariableList] :  ~(${System.lineSeparator()}$nextDepthSpace",
                                    separator = "${System.lineSeparator()}$nextDepthSpace& ",
                                    postfix = "${System.lineSeparator()}$newDepthSpace)"
                                ) { transform(it, depth + 1) }
                        }

                        is NeutralSurface, is QuestionSurface, is AnswerSurface -> throw SurfaceNotSupportedException(
                            hayesGraphElement.javaClass.name
                        )

                    }
                }

                is RdfTriple -> "triple(${transform(hayesGraphElement.rdfSubject)},${transform(hayesGraphElement.rdfPredicate)},${
                    transform(
                        hayesGraphElement.rdfObject
                    )
                })"

            }
        }

        val fofVariableList = transform(defaultPositiveSurface.graffiti)

        val (querySurfaces, questionSurface, otherHayesGraphElements) = defaultPositiveSurface.hayesGraph.fold(
            Triple<List<QuerySurface>, List<QuestionSurface>, List<HayesGraphElement>>(
                listOf(), listOf(), listOf()
            )
        ) { acc, hayesGraphElement ->
            when (hayesGraphElement) {
                is QuerySurface -> acc.copy(first = acc.first.plus(hayesGraphElement))
                is QuestionSurface -> acc.copy(second = acc.second.plus(hayesGraphElement))
                else -> acc.copy(third = acc.third.plus(hayesGraphElement))
            }
        }

        val fofQuantifiedFormula = otherHayesGraphElements.let {
            val fofFormula = if (it.isEmpty()) "\$true" else {
                if (defaultPositiveSurface.graffiti.isEmpty()) {
                    otherHayesGraphElements.joinToString(
                        prefix = "",
                        separator = "${System.lineSeparator()}$spaceBase& ",
                        postfix = ""
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                } else {
                    otherHayesGraphElements.joinToString(
                        prefix = "? [$fofVariableList] : (${System.lineSeparator()}$doubleSpaceBase",
                        separator = "${System.lineSeparator()}$doubleSpaceBase& ",
                        postfix = "${System.lineSeparator()}$spaceBase)"
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                }
            }
            "fof($tptpName,$formulaRole,${System.lineSeparator()}$spaceBase$fofFormula${System.lineSeparator()})."
        }

        val fofQuantifiedFormulaQuery = querySurfaces.mapIndexed { index, surface ->
            val fofFormula =
                if (surface.graffiti.isEmpty()) {
                    if (surface.hayesGraph.isEmpty()) "\$true" else
                        surface.hayesGraph.joinToString(
                            prefix = "",
                            separator = "${System.lineSeparator()}$spaceBase& ",
                            postfix = ""
                        ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                } else {
                    if (surface.hayesGraph.isEmpty()) "? [${transform(surface.graffiti)}] : (${System.lineSeparator()}$doubleSpaceBase\$true${System.lineSeparator()}$spaceBase)" else
                        surface.hayesGraph.joinToString(
                            prefix = "? [${transform(surface.graffiti)}] : (${System.lineSeparator()}$doubleSpaceBase",
                            separator = "${System.lineSeparator()}$doubleSpaceBase& ",
                            postfix = "${System.lineSeparator()}$spaceBase)"
                        ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                }

            "fof(query_$index,question,${System.lineSeparator()}$spaceBase$fofFormula${System.lineSeparator()})."
        }

        val fofQuantifiedFormulaQuestion = questionSurface.mapIndexed { index, surface ->
            val hayesGraphElementsWithoutAnswerSurface = surface.hayesGraph.filterNot { it is AnswerSurface }
            val fofFormula =
                if (surface.graffiti.isEmpty()) {
                    if (hayesGraphElementsWithoutAnswerSurface.isEmpty()) "\$true" else
                        hayesGraphElementsWithoutAnswerSurface.joinToString(
                            prefix = "",
                            separator = "${System.lineSeparator()}$spaceBase& ",
                            postfix = ""
                        ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                } else {
                    if (hayesGraphElementsWithoutAnswerSurface.isEmpty()) "? [${transform(surface.graffiti)}] : (${System.lineSeparator()}$doubleSpaceBase\$true${System.lineSeparator()}$spaceBase)" else
                        hayesGraphElementsWithoutAnswerSurface.joinToString(
                            prefix = "? [${transform(surface.graffiti)}] : (${System.lineSeparator()}$doubleSpaceBase",
                            separator = "${System.lineSeparator()}$doubleSpaceBase& ",
                            postfix = "${System.lineSeparator()}$spaceBase)"
                        ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                }

            "fof(question_$index,question,${System.lineSeparator()}$spaceBase$fofFormula${System.lineSeparator()})."
        }

        return try {
            buildString {
                this.append(fofQuantifiedFormula)
                if (ignoreQuerySurfaces) return@buildString

                fofQuantifiedFormulaQuery
                    .joinToString(
                        prefix = System.lineSeparator(),
                        separator = System.lineSeparator()
                    )
                    .takeUnless { fofQuantifiedFormulaQuery.isEmpty() }?.let { append(it) }

                fofQuantifiedFormulaQuestion.joinToString(
                    prefix = System.lineSeparator(),
                    separator = System.lineSeparator()
                ).takeUnless { fofQuantifiedFormulaQuestion.isEmpty() }?.let { append(it) }
            }.let { Result.Success(it) }
        } catch (exception : SurfaceNotSupportedException) {
            return Result.Error(SurfaceNotSupportedError(surface = exception.surface))
        }
    }

}

data class SurfaceNotSupportedError(val surface: String) : Error