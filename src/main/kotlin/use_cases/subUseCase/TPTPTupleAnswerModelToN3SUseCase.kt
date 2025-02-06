package use_cases.subUseCase

import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.QSurface
import use_cases.modelToString.RdfSurfaceModelToN3UseCase
import use_cases.modelTransformer.FOLGeneralTermToRDFTermUseCase
import util.InvalidInputException
import util.error.Error
import util.error.Result
import util.error.runOnSuccess

object TPTPTupleAnswerModelToRdfSurfaceUseCase {
    operator fun invoke(
        tptpTupleAnswerFormAnswer: TPTPTupleAnswerFormAnswer,
        qSurface: QSurface
    ): Result<String, Error> {

        val resultList = tptpTupleAnswerFormAnswer.answerTuples

        val rdfTransformedAnswerTuples = resultList.map { answerTuple ->
            answerTuple.map {
                FOLGeneralTermToRDFTermUseCase(it)
            }
        }

        val replacedASurface: Result<PositiveSurface, Error> = try {
            Result.Success(qSurface.replaceBlankNodes(rdfTransformedAnswerTuples))
        } catch (exc: InvalidInputException) {
            Result.Error(InvalidInputError(cause = exc))
        }
        return replacedASurface.runOnSuccess { replacedSurface ->
            RdfSurfaceModelToN3UseCase(replacedSurface)
        }
    }
}

data class InvalidInputError(val affectedFormula: String? = null, val cause: Exception) :
    AnswerTupleTransformationError
