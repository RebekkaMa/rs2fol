package app.use_cases.modelTransformer

import app.interfaces.services.jena.XSDLiteralService
import entities.rdfsurfaces.*
import entities.rdfsurfaces.rdf_term.Literal
import util.commandResult.*

class CanoncicalizeRDFSurfaceLiteralsUseCase(private val xsdLiteralService: XSDLiteralService) {

    operator fun <T : RdfSurface> invoke(rdfSurface: T): Result<T, RootError> {
        return canonicalizeHayesGraphElement(rdfSurface).map { it as T }
    }

    private fun canonicalizeHayesGraphElement(hayesGraphElement: HayesGraphElement): Result<HayesGraphElement, RootError> {
        val canonicalized = when (hayesGraphElement) {
            is RdfTriple -> {
                val subject = if (hayesGraphElement.rdfSubject is Literal) {
                    xsdLiteralService.createGeneralizedLiteral(hayesGraphElement.rdfSubject)
                        .getOrElse { return error(it) }.literal
                } else {
                    hayesGraphElement.rdfSubject
                }
                val obj = if (hayesGraphElement.rdfPredicate is Literal) {
                    xsdLiteralService.createGeneralizedLiteral(hayesGraphElement.rdfPredicate)
                        .getOrElse { return error(it) }.literal
                } else {
                    hayesGraphElement.rdfPredicate
                }
                val predicate = if (hayesGraphElement.rdfObject is Literal) {
                    xsdLiteralService.createGeneralizedLiteral(hayesGraphElement.rdfObject)
                        .getOrElse { return error(it) }.literal
                } else {
                    hayesGraphElement.rdfObject
                }

                hayesGraphElement.copy(
                    rdfSubject = subject,
                    rdfPredicate = obj,
                    rdfObject = predicate
                )
            }

            is RdfSurface -> {
                val graffiti = hayesGraphElement.graffiti
                val hayesGraph = hayesGraphElement.hayesGraph.map {
                    canonicalizeHayesGraphElement(it).getOrElse { err ->
                        return error(err)
                    }
                }
                when (hayesGraphElement) {
                    is PositiveSurface -> PositiveSurface(graffiti, hayesGraph)
                    is NegativeSurface -> NegativeSurface(graffiti, hayesGraph)
                    is NeutralSurface -> NeutralSurface(graffiti, hayesGraph)
                    is NegativeAnswerSurface -> NegativeAnswerSurface(graffiti, hayesGraph)
                    is QuerySurface -> QuerySurface(graffiti, hayesGraph)
                }
            }
        }
        return success(canonicalized)
    }
}

