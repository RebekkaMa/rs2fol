package services.parser

import entities.SZSOutputModel
import entities.SZSOutputType
import entities.SZSStatus
import entities.SZSStatusType
import interface_adapters.services.parser.SZSParserServiceError
import interface_adapters.services.parser.SZSParserServiceImpl
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import util.commandResult.getErrorOrNull
import util.commandResult.getSuccessOrNull
import kotlin.test.assertTrue

class SZSParserServiceTest : ShouldSpec({
    val parser = SZSParserServiceImpl()

    should("test valid SZS status parsing") {
        val input = """
            % SZS status Theorem for problem1
            % SZS output start Proof for problem1
            Some proof content
            % SZS output end Proof for problem1
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).first()

        assertTrue(result.isResult)
        val szsOutputModel = result.getSuccessOrNull() as? SZSOutputModel
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

        assertTrue(result.isResult)
        val szsOutputModel = result.getSuccessOrNull() as? SZSOutputModel
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

        assertTrue(result.isResult)
        val szsOutputModel = result.getSuccessOrNull() as? SZSOutputModel
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

        assertTrue(result.isResult)
        val szsStatus = result.getSuccessOrNull() as? SZSStatus
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

        assertTrue(result.isResult)
        val szsOutputModel = result.getSuccessOrNull() as? SZSOutputModel
        assertInstanceOf(SZSOutputModel::class.java, szsOutputModel)

        assertEquals(SZSStatusType.SuccessOntology.THEOREM, szsOutputModel?.status?.statusType)
        assertEquals("problem5", szsOutputModel?.identifier)
        assertEquals(null, szsOutputModel?.status?.statusDetails)
        assertEquals(SZSOutputType.PROOF, szsOutputModel?.outputType)
        assertEquals(listOf("Some proof content"), szsOutputModel?.output)
    }

    should("test multiple status blocks") {
        val input = """
            % SZS status Theorem for problem6
            % SZS output start Proof for problem6
            Some proof content
            % SZS output end Proof for problem6
            % SZS status Unsatisfiable for problem7
            % SZS output start Model for problem7
            Some model content
            % SZS output end Model for problem7
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).toList()

        assertEquals(2, result.size)

        assertInstanceOf(SZSOutputModel::class.java, result[0].getSuccessOrNull())
        assertInstanceOf(SZSOutputModel::class.java, result[1].getSuccessOrNull())

        val firstSzsOutputModel = result[0].getSuccessOrNull() as? SZSOutputModel
        val secondSzsOutputModel = result[1].getSuccessOrNull() as? SZSOutputModel

        assertEquals(SZSStatusType.SuccessOntology.THEOREM, firstSzsOutputModel?.status?.statusType)
        assertEquals("problem6", firstSzsOutputModel?.identifier)
        assertEquals(SZSOutputType.PROOF, firstSzsOutputModel?.outputType)
        assertEquals(listOf("Some proof content"), firstSzsOutputModel?.output)
        assertEquals(SZSStatusType.SuccessOntology.UNSATISFIABLE, secondSzsOutputModel?.status?.statusType)
        assertEquals("problem7", secondSzsOutputModel?.identifier)
        assertEquals(SZSOutputType.MODEL, secondSzsOutputModel?.outputType)
        assertEquals(listOf("Some model content"), secondSzsOutputModel?.output)
    }

    should("test empty file") {
        val input = ""

        val result = parser.parse(input.byteInputStream().bufferedReader()).count()

        assertEquals(0, result)
    }

    should("test nested blocks") {
        val input = """
            % SZS status Theorem for problem1
            % SZS output start Proof for problem1
            Some proof content
            % SZS output start SubProof for problem1
            Some subproof content
            % SZS output end SubProof for problem1
            % SZS output end Proof for problem1
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).single()

        assertEquals(SZSParserServiceError.OutputStartBeforeEndAndStatus, result.getErrorOrNull())
    }

    should("test block without end marker") {
        val input = """
            % SZS status Theorem for problem1
            % SZS output start Proof for problem1
            Some proof content
        """.trimIndent()

        val result = parser.parse(input.byteInputStream().bufferedReader()).single()

        assertTrue(result.isResult)
        val szsStatus = result.getSuccessOrNull() as? SZSStatus
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

        assertTrue(result.isResult)
        val szsOutputModel = result.getSuccessOrNull() as? SZSOutputModel
        assertInstanceOf(SZSOutputModel::class.java, szsOutputModel)

        assertEquals(SZSStatusType.SuccessOntology.THEOREM, szsOutputModel?.status?.statusType)
        assertEquals("problem1", szsOutputModel?.identifier)
        assertEquals(SZSOutputType.PROOF, szsOutputModel?.outputType)
        assertEquals(1000, szsOutputModel?.output?.size)
    }
})