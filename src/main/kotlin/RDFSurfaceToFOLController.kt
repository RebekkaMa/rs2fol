import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parsed

class RDFSurfaceToFOLController {

    /**
     * Transform RDF Surface graph to first-order logic axiom (and optional question) in TPTP format
     *
     * @param rdfSurfaceGraph
     * @param ignoreQuerySurface
     * @return parseError, parseResultValue
     */
    fun transformRDFSurfaceGraphToFOL(rdfSurfaceGraph: String, ignoreQuerySurface: Boolean): Pair<Boolean, String> {
        return when (
            val parserResult = RDFSurfacesParser.tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to  Transformer().transformToFOL(parserResult.value, ignoreQuerySurface)
            }
            is ErrorResult -> {
                true to ParseException(parserResult).stackTraceToString()
            }
        }
    }

    /**
     * Transform RDF Surface graph into first-order logic conjecture in TPTP format
     *
     * @param rdfSurfaceGraph
     * @return parseError, parseResultValue
     */
    fun transformRDFSurfaceGraphToFOLConjecture(rdfSurfaceGraph: String): Pair<Boolean, String> {
        return when (
            val answerParserResult = RDFSurfacesParser.tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to Transformer().transformToFOL(answerParserResult.value,false, "conjecture","conjecture")
            }
            is ErrorResult -> {
                true to ParseException(answerParserResult).stackTraceToString()
            }
        }
    }

    fun transformRDFSurfaceGraphToNotation3(rdfSurfaceGraph: String): Pair<Boolean,String>{
        return when (
            val parserResult = RDFSurfacesParser.tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to Transformer().printRDFSurfaceGraphUsingNotation3(parserResult.value)
            }
            is ErrorResult -> {
                true to ParseException(parserResult).stackTraceToString()
            }
        }
    }
}