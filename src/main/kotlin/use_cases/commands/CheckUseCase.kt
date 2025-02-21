package use_cases.commands

import entities.SZSAnswerTupleFormModel
import entities.SZSOutputModel
import entities.SZSStatus
import entities.SZSStatusType
import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.FileService
import interface_adapters.services.parser.RDFSurfaceParseService
import interface_adapters.services.parser.SZSParserService
import interface_adapters.services.theoremProver.TheoremProverRunnerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
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
        baseIri: IRI,
        configFile: Path,
        dEntailment: Boolean
    ): Flow<CommandStatus<CheckSuccess, Error>?> = channelFlow {

        val antecedentParseResult = RDFSurfaceParseService(rdfList).parseToEnd(antecedent, baseIri)
        val antecedentTPTPModels = antecedentParseResult.runOnSuccess { positiveSurface ->
            RdfSurfaceModelToTPTPModelUseCase(
                defaultPositiveSurface = positiveSurface,
                ignoreQuerySurfaces = true,
            )
        }.getOrElse {
            send(error(it))
            return@channelFlow
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
                    formulaRole = FormulaRole.Conjecture,
                    dEntailment = dEntailment
                )
            }
            .getOrElse {
                send(error(it))
                return@channelFlow
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
            reasoningTimeLimit,
            configFile
        ).getOrElse {
            send(error(it))
            return@channelFlow
        }.command

        val (vampireResult, timeoutDeferred) = TheoremProverRunnerService(
            input = antecedentFol + System.lineSeparator() + consequentFol,
            timeLimit = reasoningTimeLimit,
            command = command
        )

        launch {
            if (timeoutDeferred.await()) {
                send(success(CheckSuccess.Timeout))
                close()
            }
        }

        launch {
            SZSParserService().parse(vampireResult)
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
                                SZSStatusType.SuccessOntology.TAUTOLOGOUS_CONCLUSION_CONTRADICTORY_AXIOMS -> {
                                    send(success(CheckSuccess.Consequence))
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
                                    send(success(CheckSuccess.NoConsequence))
                                }

                                else -> send(success(CheckSuccess.NotKnown(it.statusType)))
                            }
                        }

                        is SZSAnswerTupleFormModel -> send(error(CheckError.UnknownVampireOutput))
                    }
                    close()
                }

            send(error(CheckError.UnknownVampireOutput))
            close()
        }
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