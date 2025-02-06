package interface_adapters.services

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import entities.OutputOntology
import entities.SZSData
import entities.SZSStatus

// Define the SZS Grammar
class SZSParser : Grammar<SZSData>() {
    // Tokens
    val ws by regexToken("\\s+", ignore = true) // Whitespace
    private val szsPrefix by literalToken("% SZS")
    private val szsStatus by regexToken("status [a-zA-Z0-9_]+ for [a-zA-Z0-9_]+")
    private val szsOutputStart by regexToken("output start [a-zA-Z]+")
    private val szsOutputEnd by regexToken("output end [a-zA-Z]+")
    private val nonSzsLine by regexToken("^[^%\\n][^\\n]+\\n")

    // Parsers for SZS elements
    val statusParser by (szsStatus use {
        val parts = text.split(" ")
        val statusString = parts[1].uppercase()
        val problem = parts.last()

        val status = when (statusString) {
            "THEOREM", "CONTRADICTION", "UNSATISFIABLE", "SATISFIABLE", "UNKNOWN", "COUNTERSATISFIABLE", "NOCONSEQUENCE", "TAUTOLOGY", "OPEN" -> SZSStatus.DeductiveStatus.valueOf(
                statusString
            )

            "EQUIVALENT", "EQUISATISFIABLE", "SATISFIABILITYPRESERVED", "STRONGLYSATISFIABILITYPRESERVED" -> SZSStatus.PreservingStatus.valueOf(
                statusString
            )

            "UNSOLVED", "INCOMPLETE", "RESOURCEOUT", "TIMEOUT", "ERROR", "GAVEUP" -> SZSStatus.UnsolvedStatus.valueOf(
                statusString
            )

            else -> throw IllegalArgumentException("Unknown status: $statusString")
        }

        SZSData(
            status = status,
            problem = problem
        )
    })

    val outputParser by (
            szsOutputStart * oneOrMore(nonSzsLine) * szsOutputEnd map { (start, content, end) ->
                val outputTypeString = start.text.split(" ").last().uppercase()
                val outputType = OutputOntology.valueOf(outputTypeString)
                outputType to content.text.trim()
            }
            )

    val szsLine by -szsPrefix and (statusParser and zeroOrMore(outputParser)) map { (status, outputs) ->
        status.apply {
            outputs.forEach { (type, content) ->
                this.outputs[type] = content
            }
        }
    }

    val szsFile by oneOrMore(szsLine or nonSzsLine)

    // Root parser
    override val rootParser = szsFile
}
