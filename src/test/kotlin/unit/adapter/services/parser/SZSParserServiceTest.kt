package unit.adapter.services.parser

import adapter.parser.SZSParserServiceImpl
import adapter.parser.TPTPTupleAnswerFormToModelServiceImpl
import entities.SZSOutputModel
import entities.SZSOutputType
import entities.SZSStatus
import entities.SZSStatusType
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import util.commandResult.getSuccessOrNull
import kotlin.test.assertTrue

class SZSParserServiceTest : ShouldSpec({
    val parser = SZSParserServiceImpl(
        tptpTupleAnswerFormToModelService = TPTPTupleAnswerFormToModelServiceImpl()
    )

    should("test valid SZS status parsing") {
        val input = """
            % SZS status Theorem for problem1
            % SZS output start Proof for problem1
            Some proof content
            % SZS output end Proof for problem1
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).first()

        assertTrue(result.isSuccess)
        val szsOutputModel = result.getSuccessOrNull()?.szsModel as? SZSOutputModel
        assertInstanceOf(SZSOutputModel::class.java, szsOutputModel)

        assertEquals(SZSStatusType.SuccessOntology.THEOREM, szsOutputModel?.statusType)
        assertEquals("problem1", szsOutputModel?.identifier)
        assertEquals(null, szsOutputModel?.statusDetails)
        assertEquals(SZSOutputType.PROOF, szsOutputModel?.outputType)
        assertEquals(listOf("Some proof content"), szsOutputModel?.output)
    }

    should("test valid SZS status parsing multiple output lines") {
        val input = """
            % SZS status Theorem for problem1: some details
            % SZS output start Proof for problem1
            Some proof : content
            Another content
            % SZS output end Proof for problem1
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).single()

        assertTrue(result.isSuccess)
        val szsOutputModel = result.getSuccessOrNull()?.szsModel as? SZSOutputModel
        assertInstanceOf(SZSOutputModel::class.java, szsOutputModel)


        assertEquals(SZSStatusType.SuccessOntology.THEOREM, szsOutputModel?.status?.statusType)
        assertEquals("problem1", szsOutputModel?.identifier)
        assertEquals("some details", szsOutputModel?.status?.statusDetails)
        assertEquals(SZSOutputType.PROOF, szsOutputModel?.outputType)
        assertEquals(listOf("Some proof : content", "Another content"), szsOutputModel?.output)
    }

    should("test SZS status with details parsing") {
        val input = """
            % SZS status Unsatisfiable for problem2: some details
            % SZS output start Model for problem2
            Some model content
            % SZS output end Model for problem2
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).single()

        assertTrue(result.isSuccess)
        val szsOutputModel = result.getSuccessOrNull()?.szsModel as? SZSOutputModel
        assertInstanceOf(SZSOutputModel::class.java, szsOutputModel)


        assertEquals(SZSStatusType.SuccessOntology.UNSATISFIABLE, szsOutputModel?.status?.statusType)
        assertEquals("problem2", szsOutputModel?.identifier)
        assertEquals("some details", szsOutputModel?.status?.statusDetails)
        assertEquals(SZSOutputType.MODEL, szsOutputModel?.outputType)
        assertEquals(listOf("Some model content"), szsOutputModel?.output)
    }

    should("test SZS status without output block") {
        val input = """
            % SZS status Unknown for problem3
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).single()

        assertTrue(result.isSuccess)
        val szsStatus = result.getSuccessOrNull()?.szsModel as? SZSStatus
        assertInstanceOf(SZSStatus::class.java, szsStatus)

        assertEquals(SZSStatusType.NoSuccessOntology.UNKNOWN, szsStatus?.statusType)
        assertEquals("problem3", szsStatus?.identifier)
    }

    should("test missing details") {
        val input = """
            % SZS status Theorem for problem5
            % SZS output start Proof for problem5
            Some proof content
            % SZS output end Proof for problem5
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).single()

        assertTrue(result.isSuccess)
        val szsOutputModel = result.getSuccessOrNull()?.szsModel as? SZSOutputModel
        assertInstanceOf(SZSOutputModel::class.java, szsOutputModel)

        assertEquals(SZSStatusType.SuccessOntology.THEOREM, szsOutputModel?.status?.statusType)
        assertEquals("problem5", szsOutputModel?.identifier)
        assertEquals(null, szsOutputModel?.status?.statusDetails)
        assertEquals(SZSOutputType.PROOF, szsOutputModel?.outputType)
        assertEquals(listOf("Some proof content"), szsOutputModel?.output)
    }

    should("test empty file") {
        val input = ""

        val result = parser.parse(input.byteInputStream().bufferedReader()).count()

        assertEquals(0, result)
    }

    should("test block without end marker") {
        val input = """
            % SZS status Theorem for problem1
            % SZS output start Proof for problem1
            Some proof content
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).single()

        assertTrue(result.isSuccess)
        val szsStatus = result.getSuccessOrNull()?.szsModel as? SZSStatus
        assertInstanceOf(SZSStatus::class.java, szsStatus)

        assertEquals(SZSStatusType.SuccessOntology.THEOREM, szsStatus?.statusType)
        assertEquals("problem1", szsStatus?.identifier)
    }

    should("test large file") {
        val input = buildString {
            append("% SZS status Theorem for problem1\n")
            append("% SZS output start Proof for problem1\n")
            repeat(1000) { append("Proof content line $it\n") }
            append("% SZS output end Proof for problem1\n")
        }

        val result = parser.parse(input.byteInputStream().bufferedReader()).single()

        assertTrue(result.isSuccess)
        val szsOutputModel = result.getSuccessOrNull()?.szsModel as? SZSOutputModel
        assertInstanceOf(SZSOutputModel::class.java, szsOutputModel)

        assertEquals(SZSStatusType.SuccessOntology.THEOREM, szsOutputModel?.status?.statusType)
        assertEquals("problem1", szsOutputModel?.identifier)
        assertEquals(SZSOutputType.PROOF, szsOutputModel?.outputType)
        assertEquals(1000, szsOutputModel?.output?.size)
    }
})