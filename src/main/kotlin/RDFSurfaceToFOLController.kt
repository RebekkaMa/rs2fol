import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser
import org.apache.jena.vocabulary.RDFS
import parser.RDFSurfacesParser
import rdfSurfaces.QuerySurface
import rdfSurfaces.RdfTripleElement
import util.RDFSurfacesParseException
import util.TransformerException


public sealed class RdfSurfacesResult

public class Success(val value: String, val querySurfaces: List<QuerySurface>?) : RdfSurfacesResult()

public class Failure(val failureMessage: String) : RdfSurfacesResult()

class RDFSurfaceToFOLController {

    private val generalParseErrorString = "General parse error. Please check the syntax of your RDF Surfaces graph."

    fun transformRDFSurfaceGraphToFOL(
        rdfSurfaceGraph: String,
        ignoreQuerySurface: Boolean,
        rdfLists: Boolean = false,
        verbose: Boolean = false,
    ): RdfSurfacesResult {
        return try {
            when (
                val parserResult = RDFSurfacesParser(rdfLists).tryParseToEnd(rdfSurfaceGraph)) {
                is Parsed -> Success(
                    Transformer().toFOL(parserResult.value, ignoreQuerySurface),
                    parserResult.value.getQuerySurfaces()
                )

                is ErrorResult -> Failure(ParseException(parserResult).let {
                    if (verbose) it.stackTraceToString() else generalParseErrorString
                })
            }
        } catch (exc: RDFSurfacesParseException) {
            Failure(if (verbose) exc.stackTraceToString() else (exc.message ?: exc.toString()))
        }
    }

    fun transformRDFSurfaceGraphToFOLConjecture(
        rdfSurfaceGraph: String,
        rdfLists: Boolean = false,
        verbose: Boolean = false,
    ): RdfSurfacesResult {
        return try {
            when (
                val answerParserResult = RDFSurfacesParser(rdfLists).tryParseToEnd(rdfSurfaceGraph)) {
                is Parsed -> Success(
                    Transformer().toFOL(answerParserResult.value, false, "conjecture", "conjecture"),
                    null
                )

                is ErrorResult -> Failure(ParseException(answerParserResult).let {
                    if (verbose) it.stackTraceToString() else generalParseErrorString
                })

            }
        } catch (exc: RDFSurfacesParseException) {
            Failure(if (verbose) exc.stackTraceToString() else (exc.message ?: exc.toString()))
        } catch (exc: TransformerException) {
            Failure(if (verbose) exc.stackTraceToString() else (exc.message ?: exc.toString()))
        }
    }

    fun transformRDFSurfaceGraphToNotation3(
        rdfSurfaceGraph: String,
        rdfLists: Boolean = false,
        verbose: Boolean = false,
    ): RdfSurfacesResult {
        return try {
            when (
                val parserResult = RDFSurfacesParser(rdfLists).tryParseToEnd(rdfSurfaceGraph)) {
                is Parsed -> Success(Transformer().toNotation3Sublanguage(parserResult.value), null)

                is ErrorResult -> Failure(ParseException(parserResult).let {
                    if (verbose) it.stackTraceToString() else generalParseErrorString
                })
            }
        } catch (exc: RDFSurfacesParseException) {
            Failure(if (verbose) exc.stackTraceToString() else (exc.message ?: exc.toString()))
        } catch (exc: TransformerException) {
            Failure(if (verbose) exc.stackTraceToString() else (exc.message ?: exc.toString()))
        }
    }

    fun transformQuestionAnsweringResult(resultList: Set<List<RdfTripleElement>>, querySurface: QuerySurface) =
        querySurface.replaceBlankNodes(resultList).let { Transformer().toNotation3Sublanguage(it) }

}