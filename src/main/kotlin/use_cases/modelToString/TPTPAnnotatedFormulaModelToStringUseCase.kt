package use_cases.modelToString

import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import interface_adapters.services.coder.FOLCoderService

object TPTPAnnotatedFormulaModelToStringUseCase {

    operator fun invoke(
        annotatedFormula: AnnotatedFormula
    ): String {
        val encodedFOLModel = FOLCoderService.encode(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase(encodedFOLModel)
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