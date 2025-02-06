package entities.fol

data class FOLPredicate(val name: String, val arguments: List<FOLExpression>) : FOLExpression()