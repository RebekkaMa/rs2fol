package use_cases.commands.subUseCase

import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.QSurface
import use_cases.modelToString.RdfSurfaceModelToN3UseCase
import use_cases.modelTransformer.FOLGeneralTermToRDFTermUseCase
import util.InvalidInputException
import util.commandResult.*

object TPTPTupleAnswerModelToRdfSurfaceUseCase {
    operator fun invoke(
        tptpTupleAnswerFormAnswer: TPTPTupleAnswerFormAnswer,
        qSurface: QSurface
    ): IntermediateStatus<String, Error> {

        val resultList = tptpTupleAnswerFormAnswer.answerTuples

        val rdfTransformedAnswerTuples = resultList.map { answerTuple ->
            answerTuple.map {
                FOLGeneralTermToRDFTermUseCase(it).getOrElse { err -> return intermediateError(err) }
                //TODO("Add answer tuple to error")
            }
        }

        val replacedASurface: IntermediateStatus<PositiveSurface, Error> = try {
            IntermediateStatus.Result(qSurface.replaceBlankNodes(rdfTransformedAnswerTuples))
        } catch (exc: InvalidInputException) {
            IntermediateStatus.Error(TPTPTupleAnswerModelToN3SUseCaseError.InvalidInputError(cause = exc))
        }
        return replacedASurface.runOnSuccess { replacedSurface ->
            RdfSurfaceModelToN3UseCase(replacedSurface)
        }
    }
}

sealed interface TPTPTupleAnswerModelToN3SUseCaseError : Error {
    data class InvalidInputError(val affectedFormula: String? = null, val cause: Exception) :
        TPTPTupleAnswerModelToN3SUseCaseError
}

