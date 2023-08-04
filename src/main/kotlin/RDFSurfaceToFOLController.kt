import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parsed
import parser.RDFSurfacesParser
import parser.RDFSurfacesParserRDFLists
import parser.tryParseToEnd
import rdfSurfaces.QueryRDFSurface
import rdfSurfaces.RdfTripleElement

class RDFSurfaceToFOLController {

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
        val parser = if (rdfLists) RDFSurfacesParserRDFLists else RDFSurfacesParser
        return when (
            val answerParserResult = parser.tryParseToEnd(rdfSurfaceGraph)) {
            is Parsed -> {
                false to Transformer().toFOL(answerParserResult.value, false, "conjecture", "conjecture")
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
                false to Transformer().toNotation3Sublanguage(parserResult.value)
            }

            is ErrorResult -> {
                true to ParseException(parserResult).stackTraceToString()
            }
        }
    }

    fun transformQuestionAnsweringResult(resultList: Set<List<RdfTripleElement>>, queryRDFSurface: QueryRDFSurface) =
        queryRDFSurface.replaceBlankNodes(resultList).let { Transformer().toNotation3Sublanguage(it) }

}