package entities.fol

sealed class FOLQuantifier : FOLExpression()
data class FOLForAll(val variables: List<FOLVariable>, val expression: FOLExpression) : FOLQuantifier()
data class FOLExists(val variables: List<FOLVariable>, val expression: FOLExpression) : FOLQuantifier()
