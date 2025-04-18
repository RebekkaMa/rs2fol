package app.use_cases.commands

import app.interfaces.services.SZSParserService
import app.interfaces.services.TheoremProverRunnerService
import app.use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
import app.use_cases.results.commands.CheckResult
import entities.SZSAnswerTupleFormModel
import entities.SZSOutputModel
import entities.SZSStatus
import entities.SZSStatusType
import entities.SZSStatusType.SuccessOntology.*
import entities.rdfsurfaces.rdf_term.IRI
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import util.commandResult.*
import java.nio.file.Path

class CheckUseCase(
    private val theoremProverRunnerService: TheoremProverRunnerService,
    private val szsParserService: SZSParserService,
    private val transformUseCase: TransformUseCase,
    private val getTheoremProverCommandUseCase: GetTheoremProverCommandUseCase
) {

    operator fun invoke(
        antecedent: String,
        consequent: String?,
        outputPath: Path?,
        reasoningTimeLimit: Long,
        optionId: Int,
        programName: String,
        rdfList: Boolean,
        baseIri: IRI,
        configFile: Path,
        dEntailment: Boolean,
        encode: Boolean,
    ): Flow<InfoResult<CheckResult.Success, Error>?> = channelFlow {

        val tptpFormula = transformUseCase.invoke(
            rdfSurface = antecedent,
            consequenceSurface = consequent,
            ignoreQuerySurface = true,
            useRdfLists = rdfList,
            baseIri = baseIri,
            dEntailment = dEntailment,
            outputPath = outputPath,
            encode = encode,
        ).onEach { result ->
            result.fold(
                onInfo = { send(infoInfo(it)) },
                onSuccess = { },
                onFailure = { send(infoError(it)); close() }
            )
        }.mapNotNull { it.getSuccessOrNull() }
            .firstOrNull()
            ?.res

        if (tptpFormula == null) return@channelFlow

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
                is SZSStatus, is SZSOutputModel -> send(
                    infoSuccess(
                        mapSZSStatusToCheckSuccess(
                            existingConsequent = consequent != null,
                            szsParseResult.statusType
                        )
                    )
                )

                is SZSAnswerTupleFormModel -> send(infoError(CheckResult.Error.UnknownTheoremProverOutput))
            }
        }
    }

}

private fun mapSZSStatusToCheckSuccess(existingConsequent: Boolean, status: SZSStatusType): CheckResult.Success {
    return when (status) {
        SATISFIABLE -> {
            if (existingConsequent) {
                CheckResult.Success.NoConsequence
            } else {
                CheckResult.Success.Satisfiable
            }
        }

        UNSATISFIABLE -> {
            if (existingConsequent) {
                CheckResult.Success.NoConsequence
            } else {
                CheckResult.Success.Unsatisfiable
            }
        }

        THEOREM,
        SATISFIABLE_THEOREM,
        EQUIVALENT,
        TAUTOLOGOUS_CONCLUSION,
        WEAKER_CONCLUSION,
        EQUIVALENT_THEOREM,
        TAUTOLOGY,
        WEAKER_TAUTOLOGOUS_CONCLUSION,
        WEAKER_THEOREM,
        TAUTOLOGOUS_CONCLUSION_CONTRADICTORY_AXIOMS -> CheckResult.Success.Consequence

        CONTRADICTORY_AXIOMS -> CheckResult.Success.Contradiction

        FINITELY_SATISFIABLE,
        COUNTER_SATISFIABLE,
        FINITELY_COUNTER_SATISFIABLE,
        COUNTER_THEOREM,
        SATISFIABLE_COUNTER_THEOREM,
        COUNTER_EQUIVALENT,
        UNSATISFIABLE_CONCLUSION,
        WEAKER_COUNTER_CONCLUSION,
        WEAKER_COUNTER_THEOREM,
        NO_CONSEQUENCE,
        SATISFIABLE_CONCLUSION_CONTRADICTORY_AXIOMS,
        WEAKER_CONCLUSION_CONTRADICTORY_AXIOMS,
        WEAKER_UNSATISFIABLE_CONCLUSION,
        SATISFIABLE_COUNTER_CONCLUSION_CONTRADICTORY_AXIOMS,
        UNSATISFIABLE_CONCLUSION_CONTRADICTORY_AXIOMS -> CheckResult.Success.NoConsequence

        else -> CheckResult.Success.NotKnown(status)
    }
}