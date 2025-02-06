package useCaseTest

import entities.fol.*
import use_cases.modelToString.FOLModelToStringUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class FOLModelToStringUseCaseTest {

    @Test
    fun testSimplePredicate() {
        val expression = FOLPredicate(
            name = "loves",
            arguments = listOf(FOLVariable("X"), FOLConstant("Y"))
        )
        val expected = "loves(X, 'Y')"
        val result = FOLModelToStringUseCase(expression, indent = 0)
        assertEquals(expected, result)
    }

    @Test
    fun testNestedAndExpression() {
        val expression = FOLAnd(
            expressions = listOf(
                FOLPredicate(
                    name = "parent",
                    arguments = listOf(FOLVariable("X"), FOLVariable("Y"))
                ),
                FOLOr(
                    expressions = listOf(
                        FOLPredicate(
                            name = "sibling",
                            arguments = listOf(FOLVariable("Y"), FOLVariable("Z"))
                        ),
                        FOLPredicate(
                            name = "cousin",
                            arguments = listOf(FOLVariable("Y"), FOLVariable("Z"))
                        )
                    )
                )
            )
        )
        val expected = """
(
  parent(X, Y) & 
  (
    sibling(Y, Z) | 
    cousin(Y, Z)
  )
)""".trimIndent()
        val result = FOLModelToStringUseCase(expression, indent = 0)
        assertEquals(expected, result)
    }

    @Test
    fun testComplexForAllExpression() {
        val expression = FOLForAll(
            variables = listOf(FOLVariable("X")),
            expression = FOLImplies(
                left = FOLPredicate(
                    name = "human",
                    arguments = listOf(FOLVariable("X"))
                ),
                right = FOLPredicate(
                    name = "mortal",
                    arguments = listOf(FOLVariable("X"))
                )
            )
        )
        val expected = """
![X]:
  (
    human(X) =>
    mortal(X)
  )""".trimIndent()
        val result = FOLModelToStringUseCase(expression, indent = 0)
        assertEquals(expected, result)
    }

    @Test
    fun testNegatedPredicate() {
        val expression = FOLNot(
            expression = FOLPredicate(
                name = "knows",
                arguments = listOf(FOLVariable("X"), FOLVariable("Y"))
            )
        )
        val expected = """
~(
  knows(X, Y)
)""".trimIndent()
        val result = FOLModelToStringUseCase(expression, indent = 0)
        assertEquals(expected, result)
    }
}
