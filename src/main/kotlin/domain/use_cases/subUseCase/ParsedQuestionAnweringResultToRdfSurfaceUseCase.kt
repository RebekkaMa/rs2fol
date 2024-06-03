package domain.use_cases.subUseCase

import domain.entities.PositiveSurface
import domain.entities.QSurface
import domain.entities.rdf_term.RdfTerm
import domain.error.Error
import domain.error.Result
import domain.error.runOnSuccess
import domain.use_cases.transform.RdfSurfaceModelToN3UseCase
import util.InvalidInputException

object ParsedQuestionAnweringResultToRdfSurfaceUseCase {
    operator fun invoke(resultList: Set<List<RdfTerm>>, qSurface: QSurface): Result<String, Error> {
        val replacedASurface: Result<PositiveSurface, Error> = try {
            Result.Success(qSurface.replaceBlankNodes(resultList))
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
