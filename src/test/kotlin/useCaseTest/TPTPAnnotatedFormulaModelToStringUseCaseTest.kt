package useCaseTest

import entities.fol.FOLConstant
import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import use_cases.modelToString.FOLModelToStringUseCase
import use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase

class TPTPAnnotatedFormulaModelToStringUseCaseTest {

    @Test
    fun `invoke with conjecture`() {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Conjecture, FOLConstant("testConstant"))
        val encodedFOLModel = TptpElementCoderService(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(annotatedFormula)
        assertEquals("fof(testName, conjecture, $transformedFormula).", result)
    }

    @Test
    fun `invoke with hypothesis`() {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Hypothesis, FOLConstant("testConstant"))
        val encodedFOLModel = TptpElementCoderService(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(annotatedFormula)
        assertEquals("fof(testName, hypothesis, $transformedFormula).", result)
    }

    @Test
    fun `invoke with lemma`() {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Lemma, FOLConstant("testConstant"))
        val encodedFOLModel = TptpElementCoderService(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(annotatedFormula)
        assertEquals("fof(testName, lemma, $transformedFormula).", result)
    }

    @Test
    fun `invoke with question`() {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Question, FOLConstant("testConstant"))
        val encodedFOLModel = TptpElementCoderService(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(annotatedFormula)
        assertEquals("fof(testName, question, $transformedFormula).", result)
    }
}