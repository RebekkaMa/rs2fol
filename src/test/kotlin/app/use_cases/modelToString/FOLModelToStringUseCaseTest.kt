package app.use_cases.modelToString

import entities.fol.*
import io.kotest.core.spec.style.ShouldSpec
import kotlin.test.assertEquals

class FOLModelToStringUseCaseTest : ShouldSpec({
    should("convert a simple predicate to string") {
        val expression = FOLPredicate(
            name = "loves",
            arguments = listOf(FOLVariable("X"), FOLConstant("Y"))
        )
        val expected = "loves(X, 'Y')"
        val result = FOLModelToStringUseCase().invoke(expression, indent = 0)
        assertEquals(expected, result)
    }

    should("convert a nested AND expression to string") {
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
        val result = FOLModelToStringUseCase().invoke(expression, indent = 0)
        assertEquals(expected, result)
    }

    should("convert a complex FOR ALL expression to string") {
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
        val result = FOLModelToStringUseCase().invoke(expression, indent = 0)
        assertEquals(expected, result)
    }

    should("convert a negated predicate to string") {
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
        val result = FOLModelToStringUseCase().invoke(expression, indent = 0)
        assertEquals(expected, result)
    }
})