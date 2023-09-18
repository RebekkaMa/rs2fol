import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import mu.KotlinLogging
import parser.RDFSurfacesParser
import parser.TPTPTupleAnswerFormTransformer
import rdfSurfaces.PositiveSurface
import rdfSurfaces.QuerySurface
import rdfSurfaces.RdfTripleElement
import util.TPTPTupleAnswerFormTransformerException

sealed class TPTPAnswerTupleTransformerResult

class TPTPAnswerTupleTransformerSuccess(
    val result: List<List<RdfTripleElement>>,
    val orResult: List<List<List<RdfTripleElement>>>,
) : TPTPAnswerTupleTransformerResult()

class TPTPAnswerTupleTransformerFailure(val failureMessage: String) : TPTPAnswerTupleTransformerResult()

sealed class AnswerTupleToRDFSurfacesGraphResult

class AnswerTupleToRDFSurfacesGraphSuccess(
    val value: String,
) : AnswerTupleToRDFSurfacesGraphResult()

class AnswerTupleToRDFSurfacesGraphFailure(val failureMessage: String) : AnswerTupleToRDFSurfacesGraphResult()


private val logger = KotlinLogging.logger {}

class FOLAnswerTupleToRDFSurfaceController {

    private val generalTPTPParseErrorString = "Please check the syntax of the TPTP answer tuple."
    private val generalRdfSurfacesParseErrorString = "Could not parse the RDF Surfaces graph. Please check the syntax."


    fun questionAnsweringOutputToRDFSurfacesCasc(
        querySurface: String,
        questionAnsweringOutputLines: Sequence<String>,
        debug: Boolean,
        rdfLists: Boolean,
    ): AnswerTupleToRDFSurfacesGraphResult {

        val querySurfaceParseResult = getQuerySurfaceFromRdfSurfacesGraph(querySurface, rdfLists)

        if (querySurfaceParseResult.isEmpty()) return AnswerTupleToRDFSurfacesGraphFailure("RDF Graph contains no query surface.")
        if (querySurfaceParseResult.size > 1) return AnswerTupleToRDFSurfacesGraphFailure("Multiple query surfaces are not supported.")

        return questionAnsweringOutputToRDFSurfacesCasc(
            querySurfaceParseResult.single(),
            questionAnsweringOutputLines,
            debug,
        )
    }

    fun questionAnsweringOutputToRDFSurfacesCasc(
        querySurface: QuerySurface,
        questionAnsweringOutputLines: Sequence<String>,
        debug: Boolean,
    ): AnswerTupleToRDFSurfacesGraphResult {
        val parsedResult = mutableSetOf<List<RdfTripleElement>>()
        val orResult = mutableSetOf<List<List<RdfTripleElement>>>()

        var refutationFound = false

        for (line in questionAnsweringOutputLines) {
            if (line.contains("SZS answers Tuple")) {
                val rawVampireOutputLine =
                    line.trimStart { char -> char != '[' }.trimEnd { char -> char != ']' }

                when (val parserResult = parseRawTPTPAnswerTupleList(rawVampireOutputLine, debug)) {
                    is TPTPAnswerTupleTransformerSuccess -> {
                        parsedResult.addAll(parserResult.result)
                        orResult.addAll(parserResult.orResult)
                    }

                    is TPTPAnswerTupleTransformerFailure -> {
                        return AnswerTupleToRDFSurfacesGraphFailure(parserResult.failureMessage)
                    }
                }
            }
            if (line.contains("(SZS status ContradictoryAxioms for)|(^\\s*unsat\\s*$)".toRegex(RegexOption.IGNORE_CASE))) refutationFound =
                true
        }

        if (refutationFound) {
            if (querySurface.graffiti.isEmpty()) return AnswerTupleToRDFSurfacesGraphSuccess(
                Transformer().toNotation3Sublanguage(
                    PositiveSurface(
                        emptyList(),
                        querySurface.hayesGraph
                    )
                )
            )
            return AnswerTupleToRDFSurfacesGraphSuccess("Refutation found")
        }

        val output = StringBuilder()
        if (parsedResult.isEmpty()) output.append("No answers") else
            output.append(
                transformQuestionAnsweringResult(
                    parsedResult,
                    querySurface
                )
            )
        return AnswerTupleToRDFSurfacesGraphSuccess(output.toString())
    }




    private fun transformTPTPTupleAnswerToRDFSurfaces(
        querySurface: QuerySurface,
        tptpTupleAnswer: String,
        debug: Boolean,
    ): AnswerTupleToRDFSurfacesGraphResult {

        return when (val parserResult = parseRawTPTPAnswerTupleList(tptpTupleAnswer, debug)) {
            is TPTPAnswerTupleTransformerSuccess -> {
                if (parserResult.orResult.isEmpty().not()) logger.info { parserResult.orResult }
                AnswerTupleToRDFSurfacesGraphSuccess(
                    transformQuestionAnsweringResult(
                        parserResult.result.toSet(),
                        querySurface
                    )
                )
            }

            is TPTPAnswerTupleTransformerFailure -> {
                AnswerTupleToRDFSurfacesGraphFailure(
                    parserResult.failureMessage
                )
            }
        }
    }

    fun transformTPTPTupleAnswerToRDFSurfaces(
        querySurfaceStr: String,
        tptpTupleAnswer: String,
        debug: Boolean,
        rdfLists: Boolean,
    ): AnswerTupleToRDFSurfacesGraphResult {
        return try {
            val querySurface = getQuerySurfaceFromRdfSurfacesGraph(querySurfaceStr, rdfLists)

            if (querySurface.isEmpty()) return AnswerTupleToRDFSurfacesGraphFailure("RDF Graph contains no query surface.")
            if (querySurface.size > 1) return AnswerTupleToRDFSurfacesGraphFailure("Multiple query surfaces are not supported.")

            transformTPTPTupleAnswerToRDFSurfaces(
                querySurface.single(),
                tptpTupleAnswer,
                debug,
            )

        } catch (exc: ParseException) {
            AnswerTupleToRDFSurfacesGraphFailure(if (debug) exc.stackTraceToString() else generalRdfSurfacesParseErrorString)
        }
    }


    fun transformQuestionAnsweringResult(resultList: Set<List<RdfTripleElement>>, querySurface: QuerySurface) =
        querySurface.replaceBlankNodes(resultList).let { Transformer().toNotation3Sublanguage(it) }

    private fun getQuerySurfaceFromRdfSurfacesGraph(rdfSurfacesGraph: String, rdfLists: Boolean): List<QuerySurface> {
        return RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfacesGraph).getQuerySurfaces()
    }

    private fun parseRawTPTPAnswerTupleList(
        tptpTupleAnswer: String,
        debug: Boolean,
    ): TPTPAnswerTupleTransformerResult {
        return try {

            val parserResult = TPTPTupleAnswerFormTransformer.parseToEnd(tptpTupleAnswer)
            TPTPAnswerTupleTransformerSuccess(parserResult.first, parserResult.second)

        } catch (exc: ParseException) {
            TPTPAnswerTupleTransformerFailure(
                "Affected tuple: $tptpTupleAnswer. " + (if (debug) exc.stackTraceToString() else generalTPTPParseErrorString)
            )
        } catch (exc: TPTPTupleAnswerFormTransformerException) {
            TPTPAnswerTupleTransformerFailure(
                "Affected tuple: $tptpTupleAnswer. " + (if (debug) exc.stackTraceToString() else (exc.message ?: exc.toString()))
            )
        }
    }

}