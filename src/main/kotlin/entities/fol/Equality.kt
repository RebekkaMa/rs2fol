package entities.fol

data class FOLEquality(val left: FOLExpression, val right: FOLExpression) : FOLExpression()
data class FOLNotEqual(val left: FOLExpression, val right: FOLExpression) : FOLExpression()
