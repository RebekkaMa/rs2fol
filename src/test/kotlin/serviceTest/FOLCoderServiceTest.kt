package serviceTest

import entities.fol.*
import interface_adapters.services.transforming.FOLCoderService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FOLCoderServiceTest {

    @Test
    fun `encode FOLConstant to valid TPTP literal`() {
        val folConstant = FOLConstant("testConstant")
        val result = TptpElementCoderService(folConstant)
        assertEquals(FOLConstant("\\\\u0074\\\\u0065\\\\u0073\\\\u0074\\\\u0043\\\\u006f\\\\u006e\\\\u0073\\\\u0074\\\\u0061\\\\u006e\\\\u0074"), result)
    }

    @Test
    fun `encode FOLVariable to valid TPTP variable`() {
        val folVariable = FOLVariable("testVariable")
        val result = TptpElementCoderService(folVariable)
        assertEquals(FOLVariable("Ox0074Ox0065Ox0073Ox0074Ox0056Ox0061Ox0072Ox0069Ox0061Ox0062Ox006cOx0065"), result)
    }

    @Test
    fun `encode FOLFunction with arguments`() {
        val folFunction = FOLFunction("testFunction", listOf(FOLConstant("arg1"), FOLConstant("arg2")))
        val result = TptpElementCoderService(folFunction)
        assertEquals(FOLFunction("testFunction", listOf(FOLConstant("\\\\u0061\\\\u0072\\\\u0067\\\\u0031"), FOLConstant("\\\\u0061\\\\u0072\\\\u0067\\\\u0032"))), result)
    }

    @Test
    fun `encode FOLAnd with expressions`() {
        val folAnd = FOLAnd(listOf(FOLConstant("expr1"), FOLConstant("expr2")))
        val result = TptpElementCoderService(folAnd)
        assertEquals(FOLAnd(listOf(FOLConstant("\\\\u0065\\\\u0078\\\\u0070\\\\u0072\\\\u0031"), FOLConstant("\\\\u0065\\\\u0078\\\\u0070\\\\u0072\\\\u0032"))), result)
    }

    @Test
    fun `encode FOLExists with variables and expression`() {
        val folExists = FOLExists(listOf(FOLVariable("var1")), FOLConstant("expr"))
        val result = TptpElementCoderService(folExists)
        assertEquals(FOLExists(listOf(FOLVariable("Ox0076Ox0061Ox0072Ox0031")), FOLConstant("\\\\u0065\\\\u0078\\\\u0070\\\\u0072")), result)
    }

    @Test
    fun `decode valid TPTP literal`() {
        val result = FOLCoderService.decodeLiteral("\\\\u0074\\\\u0065\\\\u0073\\\\u0074")
        assertEquals("test", result)
    }

    @Test
    fun `decode valid TPTP variable`() {
        val result = FOLCoderService.decodeVariable("Ox0074Ox0065Ox0073Ox0074")
        assertEquals("test", result)
    }
}