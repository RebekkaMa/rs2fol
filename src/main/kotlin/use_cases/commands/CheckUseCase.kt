package use_cases.commands

import entities.SZSAnswerTupleFormModel
import entities.SZSOutputModel
import entities.SZSStatus
import entities.SZSStatusType
import entities.rdfsurfaces.rdf_term.IRI
import interface_adapters.services.parser.SZSParserServiceImpl
import interface_adapters.services.theoremProver.TheoremProverRunnerServiceImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
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

        val tptpFormula = TransformUseCase.invoke(
            rdfSurface = antecedent,
            consequenceSurface = consequent,
            ignoreQuerySurface = true,
            useRdfLists = rdfList,
            baseIri = baseIri,
            dEntailment = dEntailment,
            outputPath = outputPath
        ).onEach { result ->
            result.fold(
                onInfo = { send(info(it)) },
                onSuccess = { },
                onFailure = { send(error(it)); close() }
            )
        }.mapNotNull { it.getSuccessOrNull() as TransformUseCaseSuccess }
            .first()
            .res

        val command = GetTheoremProverCommandUseCase(
            programName,
            optionId,
            reasoningTimeLimit,
            configFile
        ).getOrElse {
            send(error(it))
            close()
            return@channelFlow
        }.command

        val (vampireResult, timeoutDeferred) = TheoremProverRunnerServiceImpl(
            input = tptpFormula,
            timeLimit = reasoningTimeLimit,
            command = command
        ).getOrElse { send(error(it)); close(); return@channelFlow }

        launch {
            val szsParseResult = SZSParserServiceImpl()
                .parse(vampireResult)
                .firstOrNull()
                ?.getOrElse { send(error(it)); close(); return@launch }

            if (szsParseResult == null) {
                if (timeoutDeferred.await()) {
                    send(success(CheckSuccess.Timeout))
                    close()
                    return@launch
                }
                send(error(CheckError.UnknownTheoremProverOutput))
                close()
                return@launch
            }

            when (szsParseResult) {
                is SZSStatus, is SZSOutputModel -> {
                    when (szsParseResult.statusType) {
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
                        SZSStatusType.SuccessOntology.UNSATISFIABLE -> {
                            send(success(CheckSuccess.NoConsequence))
                        }

                        else -> send(success(CheckSuccess.NotKnown(szsParseResult.statusType)))
                    }
                }

                is SZSAnswerTupleFormModel -> send(error(CheckError.UnknownTheoremProverOutput))

            }
        }
    }

}


sealed interface CheckError : RootError {
    data object UnknownTheoremProverOutput : CheckError
    data object VampireError : CheckError
}

sealed interface CheckSuccess : Success {
    data object Consequence : CheckSuccess
    data object NoConsequence : CheckSuccess
    data class NotKnown(val szsStatusType: SZSStatusType) : CheckSuccess
    data object Timeout : CheckSuccess
}