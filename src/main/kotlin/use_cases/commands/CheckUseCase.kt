package use_cases.commands

import entities.SZSAnswerTupleFormModel
import entities.SZSOutputModel
import entities.SZSStatus
import entities.SZSStatusType
import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.SZSParser
import interface_adapters.services.parsing.RDFSurfaceParseService
import interface_adapters.services.vampire.TheoremProverService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import use_cases.GetTheoremProverCommandUseCase
import use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import use_cases.modelTransformer.FormulaRole
import use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase
import util.commandResult.*
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
    ): Flow<CommandStatus<CheckSuccess, Error>?> = flow {

        val antecedentParseResult = RDFSurfaceParseService(rdfList).parseToEnd(antecedent, baseIri)
        val antecedentTPTPModels = antecedentParseResult.runOnSuccess { positiveSurface ->
            RdfSurfaceModelToTPTPModelUseCase(
                defaultPositiveSurface = positiveSurface,
                ignoreQuerySurfaces = true,
            )
        }.getOrElse {
            emit(error(it))
            return@flow
        }
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
            .getOrElse {
                emit(error(it))
                return@flow
            }
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
        ).getOrElse {
            emit(error(it))
            return@flow
        }.command

        val vampireResult = TheoremProverService(
            input = antecedentFol + System.lineSeparator() + consequentFol,
            timeLimit = reasoningTimeLimit,
            command = command
        ) ?: kotlin.run {
            emit(success(CheckSuccess.Timeout))
            return@flow
        }

        SZSParser().parse(vampireResult)
            .singleOrNull()?.let {
                when (it) {
                    is SZSStatus, is SZSOutputModel -> {
                        when (it.statusType) {
                            SZSStatusType.SuccessOntology.THEOREM,
                            SZSStatusType.SuccessOntology.SATISFIABLE_THEOREM,
                            SZSStatusType.SuccessOntology.EQUIVALENT,
                            SZSStatusType.SuccessOntology.TAUTOLOGOUS_CONCLUSION,
                            SZSStatusType.SuccessOntology.WEAKER_CONCLUSION,
                            SZSStatusType.SuccessOntology.EQUIVALENT_THEOREM,
                            SZSStatusType.SuccessOntology.TAUTOLOGY,
                            SZSStatusType.SuccessOntology.WEAKER_TAUTOLOGOUS_CONCLUSION,
                            SZSStatusType.SuccessOntology.WEAKER_THEOREM,
                            SZSStatusType.SuccessOntology.CONTRADICTORY_AXIOMS,
                            SZSStatusType.SuccessOntology.TAUTOLOGOUS_CONCLUSION_CONTRADICTORY_AXIOMS
                                -> {
                                emit(success(CheckSuccess.Consequence))
                            }

                            SZSStatusType.SuccessOntology.SATISFIABLE,
                            SZSStatusType.SuccessOntology.FINITELY_SATISFIABLE,
                            SZSStatusType.SuccessOntology.COUNTER_SATISFIABLE,
                            SZSStatusType.SuccessOntology.FINITELY_COUNTER_SATISFIABLE,
                            SZSStatusType.SuccessOntology.COUNTER_THEOREM,
                            SZSStatusType.SuccessOntology.SATISFIABLE_COUNTER_THEOREM,
                            SZSStatusType.SuccessOntology.COUNTER_EQUIVALENT,
                            SZSStatusType.SuccessOntology.UNSATISFIABLE_CONCLUSION,
                            SZSStatusType.SuccessOntology.WEAKER_COUNTER_CONCLUSION,
                            SZSStatusType.SuccessOntology.WEAKER_COUNTER_THEOREM,
                            SZSStatusType.SuccessOntology.NO_CONSEQUENCE,
                            SZSStatusType.SuccessOntology.SATISFIABLE_CONCLUSION_CONTRADICTORY_AXIOMS,
                            SZSStatusType.SuccessOntology.WEAKER_CONCLUSION_CONTRADICTORY_AXIOMS,
                            SZSStatusType.SuccessOntology.WEAKER_UNSATISFIABLE_CONCLUSION,
                            SZSStatusType.SuccessOntology.SATISFIABLE_COUNTER_CONCLUSION_CONTRADICTORY_AXIOMS,
                            SZSStatusType.SuccessOntology.UNSATISFIABLE_CONCLUSION_CONTRADICTORY_AXIOMS,
                            SZSStatusType.SuccessOntology.UNSATISFIABLE,
                                -> {
                                emit(success(CheckSuccess.NoConsequence))
                            }

                            else -> emit(success(CheckSuccess.NotKnown(it.statusType)))
                        }
                    }

                    is SZSAnswerTupleFormModel -> emit(error(CheckError.UnknownVampireOutput))
                }
                return@flow
            }

        emit(error(CheckError.UnknownVampireOutput))
        return@flow
    }
}


sealed interface CheckError : RootError {
    data object UnknownVampireOutput : CheckError
    data object VampireError : CheckError
}

sealed interface CheckSuccess : Success {
    data object Consequence : CheckSuccess
    data object NoConsequence : CheckSuccess
    data class NotKnown(val szsStatusType: SZSStatusType) : CheckSuccess
    data object Timeout : CheckSuccess
}