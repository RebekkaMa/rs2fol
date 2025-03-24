package serviceTest

import entities.fol.*
import interface_adapters.services.coder.FOLCoderService
import io.kotest.core.spec.style.ShouldSpec
import org.junit.jupiter.api.Assertions.assertEquals

class FOLCoderServiceTest : ShouldSpec({
    should("encode FOLConstant to valid TPTP literal") {
        val folConstant = FOLConstant("testConstant")
        val result = FOLCoderService.encode(folConstant)
        assertEquals(
            FOLConstant("testConstant"),
            result
        )
    }

    should("encode FOLVariable to valid TPTP variable") {
        val folVariable = FOLVariable("testVariable")
        val result = FOLCoderService.encode(folVariable)
        assertEquals(FOLVariable("Ox0074estVariable"), result)
    }

    should("encode FOLFunction with arguments") {
        val folFunction = FOLFunction("testFunction", listOf(FOLConstant("arg1"), FOLConstant("arg2")))
        val result = FOLCoderService.encode(folFunction)
        assertEquals(
            FOLFunction(
                "testFunction",
                listOf(
                    FOLConstant("arg1"),
                    FOLConstant("arg2")
                )
            ), result
        )
    }

    should("encode FOLAnd with expressions") {
        val folAnd = FOLAnd(listOf(FOLConstant("expr1"), FOLConstant("expr2")))
        val result = FOLCoderService.encode(folAnd)
        assertEquals(
            FOLAnd(
                listOf(
                    FOLConstant("expr1"),
                    FOLConstant("expr2")
                )
            ), result
        )
    }

    should("encode FOLExists with variables and expression") {
        val folExists = FOLExists(listOf(FOLVariable("var1")), FOLConstant("expr"))
        val result = FOLCoderService.encode(folExists)
        assertEquals(
            FOLExists(
                listOf(FOLVariable("Ox0076ar1")),
                FOLConstant("expr")
            ), result
        )
    }

    should("decode valid TPTP literal") {
        val result = FOLCoderService.decodeLiteral("\\\\u0074\\\\u0065\\\\u0073\\\\u0074")
        assertEquals("test", result)
    }

    should("decode valid TPTP variable") {
        val result = FOLCoderService.decodeVariable("Ox0074Ox0065Ox0073Ox0074")
        assertEquals("test", result)
    }
})