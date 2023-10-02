package controller
import com.github.h0tk3y.betterParse.parser.ParseException
import parser.RDFSurfacesParser
import rdfSurfaces.IRI
import rdfSurfaces.QuerySurface
import util.InvalidInputException
import util.NotSupportedException
import util.TransformerException
import util.generalParseErrorString
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

class RDFSurfaceToFOLController {

    fun transformRDFSurfaceGraphToFOL(
        rdfSurfaceGraph: String,
        ignoreQuerySurface: Boolean,
        rdfLists: Boolean = false,
        baseIRI: IRI,
    ): Result<Pair<String, List<QuerySurface>>> {
        return try {
            val parserResult = RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfaceGraph, baseIRI)
            success(Transformer().toFOL(parserResult, ignoreQuerySurface) to parserResult.getQuerySurfaces())
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> failure(InvalidInputException(generalParseErrorString, exc))
                is NotSupportedException, is InvalidInputException, is TransformerException -> failure(exc)
                else -> throw exc
            }
        }
    }

    fun transformRDFSurfaceGraphToFOLConjecture(
        rdfSurfaceGraph: String,
        rdfLists: Boolean = false,
        baseIRI: IRI,
    ): Result<String> {
        return try {
            val parserResult = RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfaceGraph, baseIRI)
            success(Transformer().toFOL(parserResult, false, "conjecture", "conjecture"))
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> failure(InvalidInputException(generalParseErrorString, exc))
                is NotSupportedException, is InvalidInputException, is TransformerException -> failure(exc)
                else -> throw exc
            }
        }
    }

    fun transformRDFSurfaceGraphToNotation3(
        rdfSurfaceGraph: String,
        rdfLists: Boolean = false,
        baseIRI: IRI,
    ): Result<String> {
        return try {
            val parserResult = RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfaceGraph, baseIRI)
            success(Transformer().toNotation3Sublanguage(parserResult))
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> failure(InvalidInputException(generalParseErrorString, exc))
                is NotSupportedException, is InvalidInputException, is TransformerException -> failure(exc)
                else -> throw exc
            }
        }
    }
}