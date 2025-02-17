package interface_adapters.services

import entities.*
import interface_adapters.services.parsing.TptpTupleAnswerFormToModelService
import util.commandResult.getOrElse
import java.io.BufferedReader

class SZSParser {

    /*
        private val statusRegex = Regex("[#%]{1,2}\\s*SZS status ([\\w-]+) for ([^\\s:]+)?(?:\\s*:\\s*(.+))?")
        private val outputStartRegex = Regex("[#%]{1,2}\\s*SZS output start ([\\w-]+) for ([^\\s:]+)?(?:\\s*:\\s*(.+))?")
        private val outputEndRegex = Regex("[#%]{1,2}\\s*SZS output end ([\\w-]+) for ([^\\s:]+)?(?:\\s*:\\s*(.+))?")

        private val answerTupleRegex = Regex("[#%]{1,2}\\s*SZS answers Tuple (\\[.*\\]) for ([^\\s:]+)?(?:\\s*:\\s*(.+))?")
    */

    private val statusRegex = Regex("[#%]{1,2} SZS status ([\\w-]+)(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")
    private val outputStartRegex =
        Regex("[#%]{1,2} SZS output start ([\\w-]+)(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")
    private val outputEndRegex = Regex("[#%]{1,2} SZS output end ([\\w-]+)(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")
    private val answerTupleRegex =
        Regex("[#%]{1,2} SZS answers Tuple (\\[.*\\])(?: for ([^\\s:]+)?)?(?:\\s*:\\s*(.+))?")


    fun parse(bufferedReader: BufferedReader): List<SZSModel> {
        val models = mutableListOf<SZSModel>()
        var currentOutputType: String? = null
        var currentOutputStartDetails: String? = null
        val currentOutputData = StringBuilder()

        bufferedReader.forEachLine { line ->
            when {
                statusRegex.matches(line) -> {
                    statusRegex.find(line)?.let { matchResult ->
                        val outputType = matchResult.groupValues[1] // Immer vorhanden
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
                    if (currentOutputType != null) {
                        throw IllegalStateException("Output block not closed")
                    }
                    outputStartRegex.find(line)?.let { matchResult ->
                        val outputType = matchResult.groupValues[1] // Immer vorhanden
                        val details = matchResult.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }

                        currentOutputType = outputType
                        currentOutputStartDetails = details
                    }
                }

                outputEndRegex.matches(line) -> {
                    when {
                        currentOutputType == null -> throw IllegalStateException("Output block not opened")
                        models.lastOrNull() !is SZSStatus -> throw IllegalStateException("No previous status")
                    }

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
                            .getOrElse { throw IllegalStateException("Could not parse answer tuple") }

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
        return models
    }
}

