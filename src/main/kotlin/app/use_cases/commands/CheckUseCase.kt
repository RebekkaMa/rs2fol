package app.use_cases.commands

import app.interfaces.services.FileService
import app.interfaces.services.TheoremProverRunnerService
import app.use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
import entities.SZSAnswerTupleFormModel
import entities.SZSOutputModel
import entities.SZSStatus
import entities.SZSStatusType
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import util.commandResult.*
import java.nio.file.Path

class CheckUseCase(
    private val theoremProverRunnerService: TheoremProverRunnerService,
    private val szsParserService: app.interfaces.services.SZSParserService,
    private val fileService: FileService,
    private val transformUseCase: TransformUseCase,
    private val getTheoremProverCommandUseCase: GetTheoremProverCommandUseCase
) {

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
        dEntailment: Boolean,
    ): Flow<CommandStatus<CheckSuccess, Error>?> = channelFlow {

        val tptpFormula = transformUseCase.invoke(
            rdfSurface = antecedent,
            consequenceSurface = consequent,
            ignoreQuerySurface = true,
            useRdfLists = rdfList,
            baseIri = baseIri,
            dEntailment = dEntailment,
            outputPath = outputPath,
        ).onEach { result ->
            result.fold(
                onInfo = { send(info(it)) },
                onSuccess = { },
                onFailure = { send(error(it)); close() }
            )
        }.mapNotNull { it.getSuccessOrNull() as TransformUseCaseSuccess }
            .first()
            .res

        val command = getTheoremProverCommandUseCase(
            programName,
            optionId,
            reasoningTimeLimit,
            configFile,
        ).getOrElse {
            send(error(it))
            close()
            return@channelFlow
        }.command

        val (vampireResult, timeoutDeferred) = theoremProverRunnerService(
            input = tptpFormula,
            timeLimit = reasoningTimeLimit,
            command = command
        ).getOrElse { send(error(it)); close(); return@channelFlow }

        launch {
            val szsParseResult = szsParserService
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
                            send(success(app.use_cases.commands.CheckSuccess.Consequence))
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
    data object Consequence : app.use_cases.commands.CheckSuccess
    data object NoConsequence : app.use_cases.commands.CheckSuccess
    data class NotKnown(val szsStatusType: SZSStatusType) : app.use_cases.commands.CheckSuccess
    data object Timeout : app.use_cases.commands.CheckSuccess
}