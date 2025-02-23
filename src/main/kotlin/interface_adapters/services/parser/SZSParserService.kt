package interface_adapters.services.parser

import entities.*
import util.commandResult.*
import java.io.BufferedReader

class SZSParserService {

    private val statusRegex = Regex("[#%]{1,2} SZS status ([\\w-]+)(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")
    private val outputStartRegex =
        Regex("[#%]{1,2} SZS output start ([\\w-]+)(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")
    private val outputEndRegex = Regex("[#%]{1,2} SZS output end ([\\w-]+)(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")
    private val answerTupleRegex =
        Regex("[#%]{1,2} SZS answers Tuple (\\[.*\\])(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")


    fun parse(bufferedReader: BufferedReader): IntermediateStatus<List<SZSModel>, RootError> {
        val models = mutableListOf<SZSModel>()
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

                            models.add(
                                SZSStatus(
                                    statusType = outputType.toSZSStatusType(),
                                    identifier = identifier,
                                    statusDetails = details
                                )
                            )

                        }
                    }

                    outputStartRegex.matches(line) -> {
                        when {
                            currentOutputType != null -> return intermediateError(SZSParserServiceError.OutputStartBeforeEndAndStatus)
                            models.lastOrNull() !is SZSStatus -> return intermediateError(SZSParserServiceError.OutputStartBeforeStatus)
                        }

                        outputStartRegex.find(line)?.let { matchResult ->
                            val outputType = matchResult.groupValues[1]
                            val details = matchResult.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }

                            currentOutputType = outputType
                            currentOutputStartDetails = details
                        }
                    }

                    outputEndRegex.matches(line) -> {
                        if (currentOutputType == null) return intermediateError(SZSParserServiceError.OutputEndBeforeStart)

                        outputEndRegex.find(line)?.let { matchResult ->
                            val outputType = matchResult.groupValues[1] // Immer vorhanden
                            val details = matchResult.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }

                            val status = models.removeLastOrNull() as? SZSStatus

                            status?.let { stat ->
                                models.add(
                                    SZSOutputModel(
                                        status = stat,
                                        outputType = outputType.toSZSOutputType(),
                                        output = currentOutputData.lines().filter { it.isNotBlank() },
                                        outputStartDetails = currentOutputStartDetails,
                                        outputEndDetails = details
                                    )
                                )
                            }
                            currentOutputType = null
                            currentOutputStartDetails = null
                            currentOutputData.clear()
                        }
                    }

                    answerTupleRegex.matches(line) -> {
                        answerTupleRegex.find(line)?.let { matchResult ->
                            val answers = matchResult.groupValues[1] // Immer vorhanden
                            val details = matchResult.groupValues.getOrNull(3) // Optional

                            val status = (models.lastOrNull() as? SZSStatus)?.let {
                                models.removeLastOrNull()
                                it
                            } ?: SZSStatus(
                                SZSStatusType.SuccessOntology.SUCCESS,
                                null,
                                null
                            )

                            val tptpTupleAnswerFormAnswer = TptpTupleAnswerFormToModelService.parseToEnd(answers)
                                .getOrElse { return intermediateError(it) }

                            models.add(
                                SZSAnswerTupleFormModel(
                                    status = status,
                                    tptpTupleAnswerFormAnswer = tptpTupleAnswerFormAnswer,
                                    outputStartDetails = currentOutputStartDetails,
                                    outputEndDetails = details?.takeIf { it.isNotBlank() }
                                )
                            )
                        }

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
        return intermediateSuccess(models)
    }
}

sealed interface SZSParserServiceError : Error {
    data object OutputStartBeforeStatus : SZSParserServiceError
    data object OutputEndBeforeStart : SZSParserServiceError
    data object OutputStartBeforeEndAndStatus : SZSParserServiceError
}