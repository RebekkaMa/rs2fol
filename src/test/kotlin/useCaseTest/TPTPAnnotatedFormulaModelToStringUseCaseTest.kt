package useCaseTest

import entities.fol.FOLConstant
import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import interface_adapters.services.coder.FOLCoderService
import io.kotest.core.spec.style.ShouldSpec
import use_cases.modelToString.FOLModelToStringUseCase
import use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import kotlin.test.assertEquals

class TPTPAnnotatedFormulaModelToStringUseCaseTest : ShouldSpec({
    should("invoke with conjecture") {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Conjecture, FOLConstant("testConstant"))
        val encodedFOLModel = FOLCoderService.encode(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(annotatedFormula)
        assertEquals("fof(testName, conjecture, $transformedFormula).", result)
    }

    should("invoke with hypothesis") {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Hypothesis, FOLConstant("testConstant"))
        val encodedFOLModel = FOLCoderService.encode(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(annotatedFormula)
        assertEquals("fof(testName, hypothesis, $transformedFormula).", result)
    }

    should("invoke with lemma") {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Lemma, FOLConstant("testConstant"))
        val encodedFOLModel = FOLCoderService.encode(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(annotatedFormula)
        assertEquals("fof(testName, lemma, $transformedFormula).", result)
    }

    should("invoke with question") {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Question, FOLConstant("testConstant"))
        val encodedFOLModel = FOLCoderService.encode(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(annotatedFormula)
        assertEquals("fof(testName, question, $transformedFormula).", result)
    }
})