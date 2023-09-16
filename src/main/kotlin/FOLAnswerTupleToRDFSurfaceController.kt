import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import mu.KotlinLogging
import parser.RDFSurfacesParser
import parser.TPTPTupleAnswerFormTransformer
import rdfSurfaces.QuerySurface
import rdfSurfaces.RdfTripleElement
import util.TPTPTupleAnswerFormTransformerException

public sealed class TPTPAnswerTupleTransformerResult

public class TPTPAnswerTupleTransformerSuccess(
    val result: List<List<RdfTripleElement>>,
    val orResult: List<List<List<RdfTripleElement>>>,
) : TPTPAnswerTupleTransformerResult()

public class TPTPAnswerTupleTransformerFailure(val failureMessage: String) : TPTPAnswerTupleTransformerResult()

private val logger = KotlinLogging.logger {}

class FOLAnswerTupleToRDFSurfaceController {

    private val generalTPTPParseErrorString = "Please check the syntax of the TPTP answer tuples."
    private val generalRdfSurfacesParseErrorString = "Could not parse the RDF Surfaces graph. Please check the syntax."

    fun questionAnsweringOutputToRDFSurfacesCasc(
        querySurface: QuerySurface,
        questionAnsweringOutputLines: Sequence<String>,
        verbose: Boolean,
        quiet: Boolean,
    ): String {
        val parsedResult = mutableSetOf<List<RdfTripleElement>>()
        val rawResult = mutableSetOf<String>()
        val orResult = mutableSetOf<List<List<RdfTripleElement>>>()

        questionAnsweringOutputLines.forEach {
            if (it.contains("SZS answers Tuple")) {
                val rawVampireOutputLine =
                    it.trimStart { char -> char != '[' }.trimEnd { char -> char != ']' }

                when (val parserResult = transformTPTPTupleAnswerToRDFSurfacesElements(rawVampireOutputLine, verbose)) {
                    is TPTPAnswerTupleTransformerSuccess -> {
                        parsedResult.addAll(parserResult.result)
                        orResult.addAll(parserResult.orResult)
                    }

                    is TPTPAnswerTupleTransformerFailure -> {
                        logger.error { parserResult.failureMessage }
                        rawResult.add(rawVampireOutputLine)
                    }
                }
            }
        }
        val output = StringBuilder()
        if (rawResult.isNotEmpty()) {
            output.append(
                rawResult.joinToString(
                    prefix = "Failed to parse: ${System.lineSeparator()}-  ",
                    separator = System.lineSeparator() + "-  ",
                    postfix = System.lineSeparator()
                )
            )
        }
        if (parsedResult.isEmpty()) output.append("No answers") else
            output.append(
                transformQuestionAnsweringResult(
                    parsedResult,
                    querySurface
                )
            )
        logger.info { orResult }
        return output.toString()
    }

    private fun transformTPTPTupleAnswerToRDFSurfaces(
        querySurface: QuerySurface,
        tptpTupleAnswer: String,
        verbose: Boolean,
        quiet: Boolean,
    ): RdfSurfacesResult {

        return when (val parserResult = transformTPTPTupleAnswerToRDFSurfacesElements(tptpTupleAnswer, verbose)) {
            is TPTPAnswerTupleTransformerSuccess -> {
                if (parserResult.orResult.isEmpty().not()) logger.info { parserResult.orResult }
                Success(
                    transformQuestionAnsweringResult(
                        parserResult.result.toSet(),
                        querySurface
                    )
                )
            }

            is TPTPAnswerTupleTransformerFailure -> {
                logger.error { parserResult.failureMessage }
                Failure(
                    parserResult.failureMessage
                )
            }
        }
    }

    fun transformTPTPTupleAnswerToRDFSurfaces(
        querySurface: String,
        tptpTupleAnswer: String,
        verbose: Boolean,
        quiet: Boolean,
        rdfLists: Boolean,
    ): RdfSurfacesResult {
        return try {
            val querySurface = getQuerySurfaceFromRdfSurfacesGraph(querySurface, rdfLists)

            if (querySurface.isEmpty()) return Failure("RDF Graph contains no query surface")

            transformTPTPTupleAnswerToRDFSurfaces(
                querySurface.first(),
                tptpTupleAnswer,
                verbose,
                quiet,
            )

        } catch (exc: ParseException) {
            Failure(if (verbose) exc.stackTraceToString() else generalRdfSurfacesParseErrorString)
        }
    }

    private fun transformTPTPTupleAnswerToRDFSurfacesElements(
        tptpTupleAnswer: String,
        verbose: Boolean,
    ): TPTPAnswerTupleTransformerResult {
        return try {

            val parserResult = TPTPTupleAnswerFormTransformer.parseToEnd(tptpTupleAnswer)
            TPTPAnswerTupleTransformerSuccess(parserResult.first, parserResult.second)

        } catch (exc: ParseException) {
            TPTPAnswerTupleTransformerFailure(
                if (verbose) exc.stackTraceToString() else generalTPTPParseErrorString
            )
        } catch (exc: TPTPTupleAnswerFormTransformerException) {
            TPTPAnswerTupleTransformerFailure(
                if (verbose) exc.stackTraceToString() else (exc.message ?: exc.toString())
            )
        }
    }

    fun transformQuestionAnsweringResult(resultList: Set<List<RdfTripleElement>>, querySurface: QuerySurface) =
        querySurface.replaceBlankNodes(resultList).let { Transformer().toNotation3Sublanguage(it) }

    private fun getQuerySurfaceFromRdfSurfacesGraph(rdfSurfacesGraph: String, rdfLists: Boolean): List<QuerySurface> {
        return RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfacesGraph).getQuerySurfaces()
    }

}