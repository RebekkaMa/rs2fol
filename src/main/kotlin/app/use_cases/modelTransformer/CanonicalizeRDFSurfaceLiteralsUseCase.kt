package app.use_cases.modelTransformer

import app.interfaces.services.jena.XSDLiteralService
import entities.rdfsurfaces.*
import entities.rdfsurfaces.rdf_term.Collection
import entities.rdfsurfaces.rdf_term.Literal
import entities.rdfsurfaces.rdf_term.RDFTerm
import util.commandResult.*

class CanonicalizeRDFSurfaceLiteralsUseCase(private val xsdLiteralService: XSDLiteralService) {

    operator fun <T : RdfSurface> invoke(rdfSurface: T): Result<T, RootError> {
        return canonicalizeHayesGraphElement(rdfSurface).map { it as T }
    }

    private fun canonicalizeHayesGraphElement(hayesGraphElement: HayesGraphElement): Result<HayesGraphElement, RootError> {
        val canonicalized = when (hayesGraphElement) {
            is RDFTriple -> {
                val subject = canonicalizeHayesGraphElement(hayesGraphElement.rdfSubject).getOrElse { return error(it) }
                val predicate = canonicalizeHayesGraphElement(hayesGraphElement.rdfPredicate).getOrElse { return error(it) }
                val obj = canonicalizeHayesGraphElement(hayesGraphElement.rdfObject).getOrElse { return error(it) }
                hayesGraphElement.copy(
                    rdfSubject = subject,
                    rdfPredicate = predicate,
                    rdfObject = obj
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

    private fun canonicalizeHayesGraphElement(
        hayesGraphElement: RDFTerm
    ): Result<RDFTerm, RootError> {
        return when (hayesGraphElement) {
            is Literal -> xsdLiteralService.createGeneralizedLiteral(hayesGraphElement)
                .map { it.literal }
            is Collection -> {
                val newListElements = hayesGraphElement.list.map { element ->
                    canonicalizeHayesGraphElement(element).getOrElse { return error(it) }
                }
                success(Collection(newListElements))
            }
            else -> success(hayesGraphElement)
        }
    }
}

