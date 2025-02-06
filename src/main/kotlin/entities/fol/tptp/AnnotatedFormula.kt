package entities.fol.tptp

import entities.fol.FOLExpression

data class AnnotatedFormula(
    val name: String,
    val type: FormulaType,
    val expression: FOLExpression
)

enum class FormulaType {
    Axiom,
    Conjecture,
    Hypothesis,
    Lemma,
    Question
}