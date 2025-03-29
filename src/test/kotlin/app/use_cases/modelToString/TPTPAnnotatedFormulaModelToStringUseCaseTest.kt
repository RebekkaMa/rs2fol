package app.use_cases.modelToString

import adapter.coder.FOLCoderServiceImpl
import entities.fol.FOLConstant
import entities.fol.tptp.AnnotatedFormula
import entities.fol.tptp.FormulaType
import io.kotest.core.spec.style.ShouldSpec
import kotlin.test.assertEquals

class TPTPAnnotatedFormulaModelToStringUseCaseTest : ShouldSpec({
    val folCoderServiceImpl = FOLCoderServiceImpl()
    val folModelToStringUseCase = FOLModelToStringUseCase()
    
    should("invoke with conjecture") {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Conjecture, FOLConstant("testConstant"))
        val encodedFOLModel = folCoderServiceImpl.encode(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase().invoke(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(
            fOLModelToStringUseCase = folModelToStringUseCase,
            fOLCoderService = folCoderServiceImpl
        ).invoke(
            annotatedFormula,
            encode = false
        )
        assertEquals("fof(testName, conjecture, $transformedFormula).", result)
    }

    should("invoke with hypothesis") {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Hypothesis, FOLConstant("testConstant"))
        val encodedFOLModel = folCoderServiceImpl.encode(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase().invoke(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(
            fOLModelToStringUseCase = folModelToStringUseCase,
            fOLCoderService = folCoderServiceImpl
        ).invoke(
            annotatedFormula,
            encode = false
        )
        assertEquals("fof(testName, hypothesis, $transformedFormula).", result)
    }

    should("invoke with lemma") {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Lemma, FOLConstant("testConstant"))
        val encodedFOLModel = folCoderServiceImpl.encode(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase().invoke(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(
            fOLModelToStringUseCase = folModelToStringUseCase,
            fOLCoderService = folCoderServiceImpl
        ).invoke(
            annotatedFormula,
            encode = false
        )
        assertEquals("fof(testName, lemma, $transformedFormula).", result)
    }

    should("invoke with question") {
        val annotatedFormula = AnnotatedFormula("testName", FormulaType.Question, FOLConstant("testConstant"))
        val encodedFOLModel = folCoderServiceImpl.encode(annotatedFormula.expression)
        val transformedFormula = FOLModelToStringUseCase().invoke(encodedFOLModel)

        val result = TPTPAnnotatedFormulaModelToStringUseCase(
            fOLModelToStringUseCase = folModelToStringUseCase,
            fOLCoderService = folCoderServiceImpl
        ).invoke(
            annotatedFormula,
            encode = false
        )
        assertEquals("fof(testName, question, $transformedFormula).", result)
    }
})