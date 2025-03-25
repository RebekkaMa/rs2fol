package app.use_cases.commands

import app.interfaces.services.TheoremProverRunnerService
import app.use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
import app.use_cases.results.CheckResult
import app.use_cases.results.TransformResult
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
    ): Flow<InfoResult<CheckResult.Success, Error>?> = channelFlow {

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
                onInfo = { send(infoInfo(it)) },
                onSuccess = { },
                onFailure = { send(infoError(it)); close() }
            )
        }.mapNotNull { it.getSuccessOrNull() as TransformResult.Success }
            .first()
            .res

        val command = getTheoremProverCommandUseCase(
            programName,
            optionId,
            reasoningTimeLimit,
            configFile,
        ).getOrElse {
            send(infoError(it))
            close()
            return@channelFlow
        }.command

        val (vampireResult, timeoutDeferred) = theoremProverRunnerService(
            input = tptpFormula,
            timeLimit = reasoningTimeLimit,
            command = command
        ).getOrElse { send(infoError(it)); close(); return@channelFlow }.output

        launch {
            val szsParseResult = szsParserService
                .parse(vampireResult)
                .firstOrNull()
                ?.getOrElse { send(infoError(it)); close(); return@launch }?.szsModel

            if (szsParseResult == null) {
                if (timeoutDeferred.await()) {
                    send(infoSuccess(CheckResult.Success.Timeout))
                    close()
                    return@launch
                }
                send(infoError(CheckResult.Error.UnknownTheoremProverOutput))
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
                            send(infoSuccess(CheckResult.Success.Consequence))
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
                            send(infoSuccess(CheckResult.Success.NoConsequence))
                        }

                        else -> send(infoSuccess(CheckResult.Success.NotKnown(szsParseResult.statusType)))
                    }
                }

                is SZSAnswerTupleFormModel -> send(infoError(CheckResult.Error.UnknownTheoremProverOutput))

            }
        }
    }

}