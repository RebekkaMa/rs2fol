package controller
import com.github.h0tk3y.betterParse.parser.ParseException
import parser.RDFSurfacesParser
import model.rdf_term.IRI
import model.QSurface
import util.InvalidInputException
import util.NotSupportedException
import util.TransformerException
import util.generalParseErrorString
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

class RDFSurfaceToFOLController {

    fun transformRDFSurfaceToFOL(
        rdfSurface: String,
        ignoreQuerySurface: Boolean,
        rdfList: Boolean = false,
        baseIRI: IRI,
    ): Result<Pair<String, List<QSurface>>> {
        return try {
            val parserResult = RDFSurfacesParser(rdfList).parseToEnd(rdfSurface, baseIRI)
            success(Transformer().toFOL(parserResult, ignoreQuerySurface) to parserResult.getQSurfaces())
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> failure(InvalidInputException(generalParseErrorString, exc))
                is NotSupportedException, is InvalidInputException, is TransformerException -> failure(exc)
                else -> throw exc
            }
        }
    }

    fun transformRDFSurfaceToFOLConjecture(
        rdfSurface: String,
        rdfList: Boolean = false,
        baseIRI: IRI,
    ): Result<String> {
        return try {
            val parserResult = RDFSurfacesParser(rdfList).parseToEnd(rdfSurface, baseIRI)
            success(Transformer().toFOL(parserResult, false, "conjecture", "conjecture"))
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> failure(InvalidInputException(generalParseErrorString, exc))
                is NotSupportedException, is InvalidInputException, is TransformerException -> failure(exc)
                else -> throw exc
            }
        }
    }

    fun transformRDFSurfaceToNotation3(
        rdfSurface: String,
        rdfList: Boolean = false,
        baseIRI: IRI,
    ): Result<String> {
        return try {
            val parserResult = RDFSurfacesParser(rdfList).parseToEnd(rdfSurface, baseIRI)
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