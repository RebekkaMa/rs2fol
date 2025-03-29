package adapter.services.coder

import adapter.coder.FOLCoderServiceImpl
import entities.fol.*
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertEquals

class FOLCoderServiceTest : ShouldSpec({
    val folCoderServiceImpl = FOLCoderServiceImpl()

    context("decode and encode to TPTP Literal compatible CHAR Set") {
        should("encode and decode 1") {
            val testStr = "The first line\n" +
                    "The second line\n" +
                    "  more"
            val encoded = folCoderServiceImpl.encode(FOLConstant(testStr))
//                println(encoded)
//                println(DecodeStringToValidTPTPLiteralUseCase()(encoded))
            testStr shouldBe folCoderServiceImpl.decode(encoded)
        }

        should("encode and decode 2") {
            val testStr = "The first line\\nThe second line\\n  more"
            val encoded = folCoderServiceImpl.encode(FOLConstant(testStr))
            testStr shouldBe folCoderServiceImpl.decode(encoded)
        }

        should("encode and decode 3") {
            val testStr = "The first line\\nThe second line\\n  more"
            val encoded = folCoderServiceImpl.encode(FOLConstant(testStr))
            testStr shouldBe folCoderServiceImpl.decode(encoded)
        }
        should("encode and decode 4") {
            val testStr =
                "This is a multi-line                        # literal with embedded new lines and quotes\n" +
                        "\uD800\uDC00 literal with many quotes (\"\"\"\"\")\n" +
                        "and up to two sequential apostrophes ('')."
            val encoded = folCoderServiceImpl.encode(FOLConstant(testStr))
            testStr shouldBe folCoderServiceImpl.decode(encoded)
        }
    }
    context("decode and encode to TPTP Variable compatible CHAR Set") {
        should("encode and decode 1") {
            val testStr = "BN_1"
            val encoded = folCoderServiceImpl.encode(FOLVariable(testStr))
            testStr shouldBe folCoderServiceImpl.decode(encoded)
        }

        should("encode and decode 2") {
            val testStr = "bn_1"
            val encoded = folCoderServiceImpl.encode(FOLVariable(testStr))
            testStr shouldBe folCoderServiceImpl.decode(encoded)
        }

        should("encode and decode 3") {
            val testStr = "jiUd_.a\uD800\uDC00"
            val encoded = folCoderServiceImpl.encode(FOLVariable(testStr))
            testStr shouldBe folCoderServiceImpl.decode(encoded)
        }
        should("encode and decode 4") {
            val testStr = "Ox3A23n_3"
            val encoded = folCoderServiceImpl.encode(FOLVariable(testStr))
            testStr shouldBe folCoderServiceImpl.decode(encoded)
        }
        should("encode and decode 5") {
            val testStr = "Ox3A2P3n_3"
            val encoded = folCoderServiceImpl.encode(FOLVariable(testStr))
            testStr shouldBe folCoderServiceImpl.decode(encoded)
        }
        should("encode FOLConstant to valid TPTP literal") {
            val folConstant = FOLConstant("testConstant")
            val result = folCoderServiceImpl.encode(folConstant)
            assertEquals(
                FOLConstant("testConstant"),
                result
            )
        }

        should("encode FOLVariable to valid TPTP variable") {
            val folVariable = FOLVariable("testVariable")
            val result = folCoderServiceImpl.encode(folVariable)
            assertEquals(FOLVariable("Ox0074estVariable"), result)
        }

        should("encode FOLFunction with arguments") {
            val folFunction = FOLFunction("testFunction", listOf(FOLConstant("arg1"), FOLConstant("arg2")))
            val result = folCoderServiceImpl.encode(folFunction)
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
            val result = folCoderServiceImpl.encode(folAnd)
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
            val result = folCoderServiceImpl.encode(folExists)
            assertEquals(
                FOLExists(
                    listOf(FOLVariable("Ox0076ar1")),
                    FOLConstant("expr")
                ), result
            )
        }

        should("decode valid TPTP literal") {
            val result = folCoderServiceImpl.decode(FOLConstant("\\\\u0074\\\\u0065\\\\u0073\\\\u0074"))
            assertEquals("test", result)
        }

        should("decode valid TPTP variable") {
            val result = folCoderServiceImpl.decode(FOLConstant("Ox0074Ox0065Ox0073Ox0074"))
            assertEquals("test", result)
        }
    }
})