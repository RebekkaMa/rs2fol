package adapter.parser

import app.interfaces.results.SZSParserServiceResult
import app.interfaces.services.SZSParserService
import app.interfaces.services.TptpTupleAnswerFormParserService
import entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import util.commandResult.*
import java.io.BufferedReader

class SZSParserServiceImpl(
    private val tptpTupleAnswerFormToModelService: TptpTupleAnswerFormParserService
) : SZSParserService {

    private val statusRegex = Regex("[#%]{1,2} SZS status ([\\w-]+)(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")
    private val outputStartRegex =
        Regex("[#%]{1,2} SZS output start ([\\w-]+)(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")
    private val outputEndRegex = Regex("[#%]{1,2} SZS output end ([\\w-]+)(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")
    private val answerTupleRegex =
        Regex("[#%]{1,2} SZS answers Tuple (\\[.*\\])(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")

    override fun parse(bufferedReader: BufferedReader): Flow<Result<SZSParserServiceResult.Success.Parsed, RootError>> =
        flow {

            var szsStatus: SZSStatus? = null

            var currentOutputType: String? = null
            var currentOutputStartDetails: String? = null
            val currentOutputData = StringBuilder()

            bufferedReader.useLines { lines ->
                lines.forEach { line ->
                    when {
                        statusRegex.matches(line) -> {
                            statusRegex.find(line)?.let { matchResult ->
                                val outputType = matchResult.groupValues[1]
                                val identifier = matchResult.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }
                                val details = matchResult.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }

                                szsStatus?.let { emit(success(SZSParserServiceResult.Success.Parsed(it))) }

                                szsStatus = SZSStatus(
                                    statusType = outputType.toSZSStatusType(),
                                    identifier = identifier,
                                    statusDetails = details
                                )
                            }
                        }

                        outputStartRegex.matches(line) -> {
                            when {
                                currentOutputType != null -> {
                                    emit(error(SZSParserServiceResult.Error.OutputStartBeforeEndAndStatus))
                                    return@flow
                                }

                                szsStatus == null -> {
                                    emit(error(SZSParserServiceResult.Error.OutputStartBeforeStatus))
                                    return@flow
                                }
                            }

                            outputStartRegex.find(line)?.let { matchResult ->
                                val outputType = matchResult.groupValues[1]
                                val details = matchResult.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }

                                currentOutputType = outputType
                                currentOutputStartDetails = details
                            }
                        }

                        outputEndRegex.matches(line) -> {
                            if (currentOutputType == null) {
                                emit(error(SZSParserServiceResult.Error.OutputEndBeforeStart))
                                return@flow
                            }

                            outputEndRegex.find(line)?.let { matchResult ->
                                val outputType = matchResult.groupValues[1] // Immer vorhanden
                                val details = matchResult.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }

                                szsStatus?.let { stat ->
                                    emit(
                                        success(
                                            SZSParserServiceResult.Success.Parsed(
                                                SZSOutputModel(
                                                    status = stat,
                                                    outputType = outputType.toSZSOutputType(),
                                                    output = currentOutputData.lines().filter { it.isNotBlank() },
                                                    outputStartDetails = currentOutputStartDetails,
                                                    outputEndDetails = details
                                                )
                                            )
                                        )
                                    )
                                }

                                szsStatus = null
                                currentOutputType = null
                                currentOutputStartDetails = null
                                currentOutputData.clear()
                            }
                        }

                        answerTupleRegex.matches(line) -> {
                            answerTupleRegex.find(line)?.let { matchResult ->
                                val answers = matchResult.groupValues[1] // Immer vorhanden
                                val details = matchResult.groupValues.getOrNull(3) // Optional

                                val status = szsStatus ?: SZSStatus(
                                    SZSStatusType.SuccessOntology.SUCCESS,
                                    null,
                                    null
                                )

                                val tptpTupleAnswerFormAnswer = tptpTupleAnswerFormToModelService.parseToEnd(answers)
                                    .getOrElse {
                                        emit(error(it))
                                        return@flow
                                    }.tPTPTupleAnswerFormAnswer

                                emit(
                                    success(
                                        SZSParserServiceResult.Success.Parsed(
                                            SZSAnswerTupleFormModel(
                                                status = status,
                                                tptpTupleAnswerFormAnswer = tptpTupleAnswerFormAnswer,
                                                outputStartDetails = currentOutputStartDetails,
                                                outputEndDetails = details?.takeIf { it.isNotBlank() }
                                            )
                                        )
                                    )
                                )

                            }

                            szsStatus = null
                            currentOutputType = null
                            currentOutputStartDetails = null
                            currentOutputData.clear()
                        }

                        currentOutputType != null && line.isNotBlank() -> {
                            currentOutputData.appendLine(line)
                        }

                        else -> Unit
                    }

                }
            }
            szsStatus?.let { emit(success(SZSParserServiceResult.Success.Parsed(it))) }
        }
}