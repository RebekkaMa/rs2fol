import com.github.h0tk3y.betterParse.parser.ParseException
import parser.RDFSurfacesParser
import rdfSurfaces.IRI
import rdfSurfaces.QuerySurface
import util.RDFSurfacesParseException
import util.TransformerException


sealed class RdfSurfacesResult
class Success(val value: String, val querySurfaces: List<QuerySurface>? = null) : RdfSurfacesResult()
class Failure(val failureMessage: String) : RdfSurfacesResult()

class RDFSurfaceToFOLController {

    private val generalParseErrorString = "Please check the syntax of your RDF surfaces graph."

    fun transformRDFSurfaceGraphToFOL(
        rdfSurfaceGraph: String,
        ignoreQuerySurface: Boolean,
        rdfLists: Boolean = false,
        verbose: Boolean = false,
        baseIRI: IRI
    ): RdfSurfacesResult {
        return try {
            val parserResult = RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfaceGraph, baseIRI)
            Success(
                Transformer().toFOL(parserResult, ignoreQuerySurface),
                parserResult.getQuerySurfaces()
            )
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> Failure(if (verbose) exc.stackTraceToString() else generalParseErrorString)
                is RDFSurfacesParseException, is TransformerException -> Failure(
                    if (verbose) exc.stackTraceToString() else (exc.message ?: exc.toString())
                )

                else -> throw exc
            }
        }
    }

    fun transformRDFSurfaceGraphToFOLConjecture(
        rdfSurfaceGraph: String,
        rdfLists: Boolean = false,
        verbose: Boolean = false,
        baseIRI: IRI
    ): RdfSurfacesResult {
        return try {
            val parserResult = RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfaceGraph, baseIRI)
            Success(
                Transformer().toFOL(parserResult, false, "conjecture", "conjecture"),
                null
            )
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> Failure(if (verbose) exc.stackTraceToString() else generalParseErrorString)
                is RDFSurfacesParseException, is TransformerException -> Failure(
                    if (verbose) exc.stackTraceToString() else (exc.message ?: exc.toString())
                )

                else -> throw exc
            }
        }
    }

    fun transformRDFSurfaceGraphToNotation3(
        rdfSurfaceGraph: String,
        rdfLists: Boolean = false,
        verbose: Boolean = false,
        baseIRI: IRI
    ): RdfSurfacesResult {
        return try {
            val parserResult = RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfaceGraph, baseIRI)
            Success(Transformer().toNotation3Sublanguage(parserResult), parserResult.getQuerySurfaces())

        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> Failure(if (verbose) exc.stackTraceToString() else generalParseErrorString)
                is RDFSurfacesParseException, is TransformerException -> Failure(
                    if (verbose) exc.stackTraceToString() else (exc.message ?: exc.toString())
                )

                else -> throw exc
            }
        }
    }
}