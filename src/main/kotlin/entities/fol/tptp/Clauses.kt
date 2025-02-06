package entities.fol.tptp

import entities.fol.FOLExpression

data class TPTPClause(
    val name: String,
    val type: ClauseType,
    val literals: List<FOLExpression>
)

enum class ClauseType {
    Axiom,
    Conjecture,
    Hypothesis
}
