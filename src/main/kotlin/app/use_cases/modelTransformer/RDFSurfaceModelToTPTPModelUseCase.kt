package app.use_cases.modelTransformer

import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import entities.rdfsurfaces.PositiveSurface
import util.commandResult.Result
import util.commandResult.RootError
import util.commandResult.getOrElse

class RDFSurfaceModelToTPTPModelUseCase(private val rdfSurfaceModelToFOLModelUseCase: RDFSurfaceModelToFOLModelUseCase) {
    operator fun invoke(
        defaultPositiveSurface: PositiveSurface,
        ignoreQuerySurfaces: Boolean = false,
        formulaRole: FormulaRole = FormulaRole.Axiom,
        tptpName: String = formulaRole.name.lowercase(),
        listType: ListType = ListType.FUNCTION
    ): Result<List<AnnotatedFormula>, RootError> {

        val (formula, queryFormula) = rdfSurfaceModelToFOLModelUseCase.invoke(
            defaultPositiveSurface = defaultPositiveSurface,
            listType = listType
        ).getOrElse {
            return util.commandResult.error(it)
        }

        val annotatedFormula = AnnotatedFormula(
            name = tptpName,
            type = formulaRole.toFormulaType(),
            expression = formula
        )

        val annotatedQueryFormula = queryFormula.mapIndexed { index, surface ->
            AnnotatedFormula(
                name = "query_$index",
                type = FormulaType.Question,
                expression = surface
            )
        }

        return Result.Success(
            buildList {
                add(annotatedFormula)
                if (ignoreQuerySurfaces) return@buildList
                addAll(annotatedQueryFormula)
            }
        )
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