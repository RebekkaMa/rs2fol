import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parsed
import parser.RDFSurfacesParser
import rdfSurfaces.QuerySurface
import rdfSurfaces.RdfTripleElement

class RDFSurfaceToFOLController {

    fun transformRDFSurfaceGraphToFOL(
        rdfSurfaceGraph: String,
        ignoreQuerySurface: Boolean,
        rdfLists: Boolean = false
    ): Triple<Boolean, String, List<QuerySurface>?> {
        return when (
            val parserResult = RDFSurfacesParser(rdfLists).tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                Triple(
                    false,
                    Transformer().toFOL(parserResult.value, ignoreQuerySurface),
                    parserResult.value.getQuerySurfaces()
                )
            }

            is ErrorResult -> {
                Triple(true, ParseException(parserResult).stackTraceToString(), null)
            }
        }
    }

    fun transformRDFSurfaceGraphToFOLConjecture(
        rdfSurfaceGraph: String,
        rdfLists: Boolean = false
    ): Pair<Boolean, String> {
        return when (
            val answerParserResult = RDFSurfacesParser(rdfLists).tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to Transformer().toFOL(answerParserResult.value, false, "conjecture", "conjecture")
            }

            is ErrorResult -> {
                true to ParseException(answerParserResult).stackTraceToString()
            }
        }
    }

    fun transformRDFSurfaceGraphToNotation3(rdfSurfaceGraph: String, rdfLists: Boolean = false): Pair<Boolean, String> {
        return when (
            val parserResult = RDFSurfacesParser(rdfLists).tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to Transformer().toNotation3Sublanguage(parserResult.value)
            }

            is ErrorResult -> {
                true to ParseException(parserResult).stackTraceToString()
            }
        }
    }

    fun transformQuestionAnsweringResult(resultList: Set<List<RdfTripleElement>>, querySurface: QuerySurface) =
        querySurface.replaceBlankNodes(resultList).let { Transformer().toNotation3Sublanguage(it) }

}