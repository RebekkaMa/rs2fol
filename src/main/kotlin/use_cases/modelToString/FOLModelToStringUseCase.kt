package use_cases.modelToString

import entities.fol.*

object FOLModelToStringUseCase {

    operator fun invoke(
        folExpression: FOLExpression,
        indent: Int = 0
    ): String {
        val padding = "  ".repeat(indent)
        val newLine = System.lineSeparator()

        return when (folExpression) {
            is FOLVariable -> folExpression.name

            is FOLFunction -> {
                val args = folExpression.arguments.joinToString(", ") { invoke(it, 0) }
                "${folExpression.name}($args)"
            }

            FOLFalse -> "\$false"

            FOLTrue -> "\$true"


            is FOLConstant -> "'${folExpression.name}'"

            is FOLPredicate -> {
                val args = folExpression.arguments.joinToString(", ") { invoke(it, 0) }
                "${folExpression.name}($args)"
            }

            is FOLAnd -> folExpression.expressions.joinToString(
                prefix = "($newLine$padding  ",
                postfix = "$newLine$padding)",
                separator = " & $newLine$padding  "
            ) { invoke(it, indent + 1) }

            is FOLOr -> folExpression.expressions.joinToString(
                prefix = "($newLine$padding  ",
                postfix = "$newLine$padding)",
                separator = " | $newLine$padding  "
            ) { invoke(it, indent + 1) }

            is FOLNot -> "~($newLine$padding  ${invoke(folExpression.expression, indent + 1)}$newLine$padding)"

            is FOLForAll -> {
                "![${folExpression.variables.joinToString(separator = ", ") { it.name }}]:$newLine$padding  " +
                        invoke(folExpression.expression, indent + 1)
            }

            is FOLExists -> {
                "?[${folExpression.variables.joinToString(separator = ", ") { it.name }}]:$newLine$padding  " +
                        invoke(folExpression.expression, indent + 1)
            }

            is FOLEquality -> {
                "($newLine$padding  ${
                    invoke(
                        folExpression.left,
                        indent + 1
                    )
                } =$newLine$padding  ${invoke(folExpression.right, indent + 1)}$newLine$padding)"
            }

            is FOLEquivalent -> {
                "($newLine$padding  ${invoke(folExpression.left, indent + 1)} <=>$newLine$padding  ${
                    invoke(
                        folExpression.right,
                        indent + 1
                    )
                }$newLine$padding)"
            }

            is FOLImplies -> {
                "($newLine$padding  ${
                    invoke(
                        folExpression.left,
                        indent + 1
                    )
                } =>$newLine$padding  ${invoke(folExpression.right, indent + 1)}$newLine$padding)"
            }

            is FOLNotEqual -> {
                "($newLine$padding  ${
                    invoke(
                        folExpression.left,
                        indent + 1
                    )
                } !=$newLine$padding  ${invoke(folExpression.right, indent + 1)}$newLine$padding)"
            }
        }
    }
}

