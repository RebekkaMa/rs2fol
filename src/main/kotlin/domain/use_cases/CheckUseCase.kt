package domain.use_cases

import domain.entities.rdf_term.IRI
import domain.error.*
import domain.use_cases.transform.RdfSurfaceModelToFolUseCase
import interface_adapters.services.FileService
import interface_adapters.services.parsing.RDFSurfaceParseService
import interface_adapters.services.vampire.VampireService
import java.nio.file.Path

object CheckUseCase {

    operator fun invoke(
        antecedent: String,
        consequent: String,
        outputPath: Path?,
        reasoningTimeLimit: Long,
        vampireMode: Int,
        vampireExecutable: Path,
        rdfList: Boolean,
        baseIri: IRI
    ) : Result<CheckSuccess, Error>?{

        val antecedentParseResult = RDFSurfaceParseService(rdfList).parseToEnd(antecedent, baseIri)
        val antecedentFol = antecedentParseResult.runOnSuccess { positiveSurface ->
            RdfSurfaceModelToFolUseCase(
                defaultPositiveSurface = positiveSurface,
                ignoreQuerySurfaces = true,
            )
        }.getOrElse { return error(it) }

        val consequentParseResult = RDFSurfaceParseService(rdfList).parseToEnd(consequent, baseIri)
        val consequentFol = consequentParseResult.runOnSuccess {
            positiveSurface ->
                RdfSurfaceModelToFolUseCase(
                    defaultPositiveSurface = positiveSurface,
                    ignoreQuerySurfaces = false,
                    tptpName = "conjecture",
                    formulaRole = "conjecture"
                )
        }.getOrElse { return error(it) }


        outputPath?.let {
            FileService.createNewFile(
                path = outputPath,
                content = antecedentFol + System.lineSeparator() + consequentFol
            )
        }

        val vampireResult = VampireService.startForConsequenceChecking(
            folConjecture = consequentFol,
            folAtom = antecedentFol,
            timeLimit = reasoningTimeLimit,
            vampireOption = vampireMode,
            vampireExec = vampireExecutable
        ) ?: kotlin.run { return success(CheckSuccess.Timeout) }

        vampireResult.lastOrNull {
            it == "sat" || it == "unsat" || it.contains("error", ignoreCase = true)
        }?.let {
           return when(it) {
                "sat" -> success(CheckSuccess.NoConsequence)
                "unsat" -> success(CheckSuccess.Consequence)
                "error" -> error(CheckError.VampireError)
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