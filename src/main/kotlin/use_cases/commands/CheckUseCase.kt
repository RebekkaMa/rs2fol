package use_cases.commands

import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import interface_adapters.services.vampire.TheoremProverService
import use_cases.GetTheoremProverCommandUseCase
import use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import use_cases.modelTransformer.FormulaRole
import use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import util.error.*
import java.nio.file.Path

object CheckUseCase {

    operator fun invoke(
        antecedent: String,
        consequent: String,
        outputPath: Path?,
        reasoningTimeLimit: Long,
        optionId: Int,
        programName: String,
        rdfList: Boolean,
        baseIri: IRI
    ): Result<CheckSuccess, Error>? {

        val antecedentParseResult = RDFSurfaceParseService(rdfList).parseToEnd(antecedent, baseIri)
        val antecedentTPTPModels = antecedentParseResult.runOnSuccess { positiveSurface ->
            RdfSurfaceModelToTPTPModelUseCase(
                defaultPositiveSurface = positiveSurface,
                ignoreQuerySurfaces = true,
            )
        }.getOrElse { return error(it) }
        val antecedentFol =
            antecedentTPTPModels.joinToString(separator = System.lineSeparator()) {
                TPTPAnnotatedFormulaModelToStringUseCase(
                    it
                )
            }

        val consequentParseResult = RDFSurfaceParseService(rdfList).parseToEnd(consequent, baseIri)
        val consequentFol = consequentParseResult
            .runOnSuccess { positiveSurface ->
                RdfSurfaceModelToTPTPModelUseCase(
                    defaultPositiveSurface = positiveSurface,
                    ignoreQuerySurfaces = false,
                    tptpName = "conjecture",
                    formulaRole = FormulaRole.Conjecture
                )
            }
            .getOrElse { return error(it) }
            .joinToString(separator = System.lineSeparator()) { TPTPAnnotatedFormulaModelToStringUseCase(it) }

        outputPath?.let {
            FileService.createNewFile(
                path = outputPath,
                content = antecedentFol + System.lineSeparator() + consequentFol
            )
        }

        val command = GetTheoremProverCommandUseCase(
            programName,
            optionId,
            reasoningTimeLimit
        ).getOrElse { return error(it) }.command

        val vampireResult = TheoremProverService(
            input = antecedent + System.lineSeparator() + consequent,
            timeLimit = reasoningTimeLimit,
            command = command
        ) ?: kotlin.run { return success(CheckSuccess.Timeout) }

        vampireResult.readLines().lastOrNull {
            it == "sat" || it == "unsat" || it.contains("util/error", ignoreCase = true)
        }?.let {
            return when (it) {
                "sat" -> success(CheckSuccess.NoConsequence)
                "unsat" -> success(CheckSuccess.Consequence)
                "util/error" -> error(CheckError.VampireError)
                else -> error(CheckError.UnknownVampireOutput)
            }
        }
        return null
    }
}


sealed interface CheckError : RootError {
    data object UnknownVampireOutput : CheckError
    data object VampireError : CheckError
}

sealed interface CheckSuccess : Success {
    data object Consequence : CheckSuccess
    data object NoConsequence : CheckSuccess
    data object Timeout : CheckSuccess
}