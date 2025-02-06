package entities.fol

sealed class FOLConnective : FOLExpression()

data class FOLAnd(val expressions : List<FOLExpression>) : FOLConnective()
data class FOLOr(val expressions : List<FOLExpression>) : FOLConnective()
data class FOLNot(val expression: FOLExpression) : FOLConnective()
data class FOLImplies(val left: FOLExpression, val right: FOLExpression) : FOLConnective()
data class FOLEquivalent(val left: FOLExpression, val right: FOLExpression) : FOLConnective()