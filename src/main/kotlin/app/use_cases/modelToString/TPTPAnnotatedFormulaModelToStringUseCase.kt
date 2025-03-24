package app.use_cases.modelToString

import adapter.coder.FOLCoderService
import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType

class TPTPAnnotatedFormulaModelToStringUseCase(
    private val fOLModelToStringUseCase: FOLModelToStringUseCase,
    private val fOLCoderService: FOLCoderService
) {

    operator fun invoke(
        annotatedFormula: AnnotatedFormula
    ): String {
        val encodedFOLModel = fOLCoderService.encode(annotatedFormula.expression)
        val transformedFormula = fOLModelToStringUseCase.invoke(encodedFOLModel)
        when (annotatedFormula.type) {
            FormulaType.Axiom -> {
                return "fof(${annotatedFormula.name}, axiom, $transformedFormula)."
            }

            FormulaType.Conjecture -> {
                return "fof(${annotatedFormula.name}, conjecture, $transformedFormula)."
            }

            FormulaType.Hypothesis -> {
                return "fof(${annotatedFormula.name}, hypothesis, $transformedFormula)."
            }

            FormulaType.Lemma -> {
                return "fof(${annotatedFormula.name}, lemma, $transformedFormula)."
            }

            FormulaType.Question -> {
                return "fof(${annotatedFormula.name}, question, $transformedFormula)."
            }
        }
    }
}