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
            val parserResult = N3sToFolParser.tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to (N3sToFolParser.createFofAnnotatedAxiom(parserResult.value.first) + (if (ignoreQuerySurface) "" else ("\n" + N3sToFolParser.createFofAnnotatedQuestion(
                    parserResult.value.second
                ))))
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
            val answerParserResult = N3sToFolParser.tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to N3sToFolParser.createFofAnnotatedConjecture(answerParserResult.value.first)
            }
            is ErrorResult -> {
                true to ParseException(answerParserResult).stackTraceToString()
            }
        }
    }
}