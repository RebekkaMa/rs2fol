package app.use_cases.commands.subUseCase

import app.use_cases.modelToString.RdfSurfaceModelToN3UseCase
import app.use_cases.modelTransformer.CanonicalizeRDFSurfaceLiteralsUseCase
import app.use_cases.modelTransformer.FOLGeneralTermToRDFTermUseCase
import app.use_cases.results.subUseCaseResults.TPTPTupleAnswerModelToN3SResult
import entities.fol.tptp.AnswerTuple
import entities.rdfsurfaces.QSurface
import util.commandResult.*

class TPTPTupleAnswerModelToN3SUseCase(
    private val rdfSurfaceModelToN3UseCase: RdfSurfaceModelToN3UseCase,
    private val fOLGeneralTermToRDFTermUseCase: FOLGeneralTermToRDFTermUseCase,
    private val canonicalizeRDFSurfaceLiteralsUseCase: CanonicalizeRDFSurfaceLiteralsUseCase
) {
    operator fun invoke(
        answerTuples: List<AnswerTuple>,
        qSurface: QSurface,
        dEntailment: Boolean = false,
        encode: Boolean = false
    ): Result<String, Error> {

        val rdfTransformedAnswerTuples = answerTuples.map { answerTuple ->
            answerTuple.map {
                fOLGeneralTermToRDFTermUseCase.invoke(it).getOrElse { err ->
                    return error(
                        TPTPTupleAnswerModelToN3SResult.Error.TransformationError(
                            affectedFormula = answerTuple.toString(),
                            error = err
                        )
                    )
                }
            }
        }

       return qSurface.replaceBlankNodes(rdfTransformedAnswerTuples).fold(
            onSuccess = { surface ->
                val sur = if (dEntailment) {
                    canonicalizeRDFSurfaceLiteralsUseCase.invoke(surface).getOrElse { err -> return error(err) }
                } else surface
                rdfSurfaceModelToN3UseCase.invoke(
                    defaultPositiveSurface = sur,
                    encode = encode
                )
            },
            onFailure = {
                error(
                    TPTPTupleAnswerModelToN3SResult.Error.TransformationError(
                        affectedFormula = answerTuples.toString(),
                        error = it
                    )
                )
            }
        )
    }
}