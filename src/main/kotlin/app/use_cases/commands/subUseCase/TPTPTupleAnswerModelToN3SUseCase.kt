package app.use_cases.commands.subUseCase

import app.use_cases.modelToString.RdfSurfaceModelToN3UseCase
import app.use_cases.modelTransformer.FOLGeneralTermToRDFTermUseCase
import entities.fol.tptp.AnswerTuple
import entities.rdfsurfaces.PositiveSurface
import entities.rdfsurfaces.QSurface
import util.InvalidInputException
import util.commandResult.*

class TPTPTupleAnswerModelToRdfSurfaceUseCase(
    private val rdfSurfaceModelToN3UseCase: RdfSurfaceModelToN3UseCase,
    private val fOLGeneralTermToRDFTermUseCase: FOLGeneralTermToRDFTermUseCase
) {
    operator fun invoke(
        answerTuples: List<AnswerTuple>,
        qSurface: QSurface
    ): IntermediateStatus<String, Error> {

        val rdfTransformedAnswerTuples = answerTuples.map { answerTuple ->
            answerTuple.map {
                fOLGeneralTermToRDFTermUseCase.invoke(it).getOrElse { err -> return intermediateError(err) }
                //TODO("Add answer tuple to error")
            }
        }

        val replacedASurface: IntermediateStatus<PositiveSurface, Error> = try {
            IntermediateStatus.Result(qSurface.replaceBlankNodes(rdfTransformedAnswerTuples))
        } catch (exc: InvalidInputException) {
            IntermediateStatus.Error(
                TPTPTupleAnswerModelToN3SUseCaseError.InvalidInputError(
                    cause = exc
                )
            )
        }
        return replacedASurface.runOnSuccess { replacedSurface ->
            rdfSurfaceModelToN3UseCase.invoke(replacedSurface)
        }
    }
}

sealed interface TPTPTupleAnswerModelToN3SUseCaseError : Error {
    data class InvalidInputError(val affectedFormula: String? = null, val cause: Exception) :
        TPTPTupleAnswerModelToN3SUseCaseError
}

