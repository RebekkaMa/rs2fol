package use_cases.subUseCase

import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.QSurface
import use_cases.modelToString.RdfSurfaceModelToN3UseCase
import use_cases.modelTransformer.FOLGeneralTermToRDFTermUseCase
import util.InvalidInputException
import util.commandResult.Error
import util.commandResult.IntermediateStatus
import util.commandResult.runOnSuccess

object TPTPTupleAnswerModelToRdfSurfaceUseCase {
    operator fun invoke(
        tptpTupleAnswerFormAnswer: TPTPTupleAnswerFormAnswer,
        qSurface: QSurface
    ): IntermediateStatus<String, Error> {

        val resultList = tptpTupleAnswerFormAnswer.answerTuples

        val rdfTransformedAnswerTuples = resultList.map { answerTuple ->
            answerTuple.map {
                FOLGeneralTermToRDFTermUseCase(it)
            }
        }

        val replacedASurface: IntermediateStatus<PositiveSurface, Error> = try {
            IntermediateStatus.Result(qSurface.replaceBlankNodes(rdfTransformedAnswerTuples))
        } catch (exc: InvalidInputException) {
            IntermediateStatus.Error(InvalidInputError(cause = exc))
        }
        return replacedASurface.runOnSuccess { replacedSurface ->
            RdfSurfaceModelToN3UseCase(replacedSurface)
        }
    }
}

data class InvalidInputError(val affectedFormula: String? = null, val cause: Exception) :
    AnswerTupleTransformationError
