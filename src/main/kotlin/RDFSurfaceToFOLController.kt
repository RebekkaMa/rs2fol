import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parsed
import parser.RDFSurfacesParser
import parser.RDFSurfacesParserRDFLists
import rdfSurfaces.QueryRDFSurface
import rdfSurfaces.RdfTripleElement

class RDFSurfaceToFOLController {

    /**
     * Transform RDF Surface graph to first-order logic axiom (and optional question) in TPTP format
     *
     * @param rdfSurfaceGraph
     * @param ignoreQuerySurface
     * @return parseError, parseResultValue
     */
    fun transformRDFSurfaceGraphToFOL(
        rdfSurfaceGraph: String,
        ignoreQuerySurface: Boolean,
        rdfLists: Boolean = false
    ): Triple<Boolean, String, List<QueryRDFSurface>?> {
        val parser = if (rdfLists) RDFSurfacesParserRDFLists else RDFSurfacesParser
        return when (
            val parserResult = parser.tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                Triple(
                    false,
                    Transformer().transformToFOL(parserResult.value, ignoreQuerySurface),
                    parserResult.value.getQuerySurfaces()
                )
            }

            is ErrorResult -> {
                Triple(true, ParseException(parserResult).stackTraceToString(), null)
            }
        }
    }

    /**
     * Transform RDF Surface graph into first-order logic conjecture in TPTP format
     *
     * @param rdfSurfaceGraph
     * @return parseError, parseResultValue
     */
    fun transformRDFSurfaceGraphToFOLConjecture(
        rdfSurfaceGraph: String,
        rdfLists: Boolean = false
    ): Pair<Boolean, String> {
        val parser = if (rdfLists) RDFSurfacesParserRDFLists else RDFSurfacesParser
        return when (
            val answerParserResult = parser.tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to Transformer().transformToFOL(answerParserResult.value, false, "conjecture", "conjecture")
            }

            is ErrorResult -> {
                true to ParseException(answerParserResult).stackTraceToString()
            }
        }
    }

    fun transformRDFSurfaceGraphToNotation3(rdfSurfaceGraph: String, rdfLists: Boolean = false): Pair<Boolean, String> {
        val parser = if (rdfLists) RDFSurfacesParserRDFLists else RDFSurfacesParser
        return when (
            val parserResult = parser.tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to Transformer().printUsingNotation3(parserResult.value)
            }

            is ErrorResult -> {
                true to ParseException(parserResult).stackTraceToString()
            }
        }
    }

    fun transformQuestionAnsweringResult(resultList: Set<List<RdfTripleElement>>, queryRDFSurface: QueryRDFSurface) =
        queryRDFSurface.replaceBlankNodes(resultList).let { Transformer().printUsingNotation3(it) }

}