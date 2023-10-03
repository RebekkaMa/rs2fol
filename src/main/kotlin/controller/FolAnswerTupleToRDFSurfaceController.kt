package controller
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import mu.KotlinLogging
import parser.RDFSurfacesParser
import parser.TptpTupleAnswerFormTransformer
import rdfSurfaces.IRI
import rdfSurfaces.PositiveSurface
import rdfSurfaces.QuerySurface
import rdfSurfaces.RdfTripleElement
import util.InvalidInputException
import util.NotSupportedException
import util.generalParseErrorString

private val logger = KotlinLogging.logger {}

class FolAnswerTupleToRDFSurfaceController {

    fun getQuerySurfaceFromRdfSurface(
        rdfSurfacesGraph: String,
        baseIRI: IRI,
        rdfLists: Boolean,
    ): Result<List<QuerySurface>> {
        return try {
            Result.success(RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfacesGraph, baseIRI).getQuerySurfaces())
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> Result.failure(InvalidInputException(generalParseErrorString, exc))
                is InvalidInputException, is NotSupportedException -> Result.failure(exc)
                else -> throw exc
            }
        }
    }

    fun questionAnsweringOutputToRDFSurfacesCasc(
        querySurface: QuerySurface,
        questionAnsweringOutputLines: Sequence<String>,
    ): Result<String> {
        val parsedResult = mutableSetOf<List<RdfTripleElement>>()

        var refutationFound = false

        for (line in questionAnsweringOutputLines) {
            if (line.contains("SZS answers Tuple")) {
                val tptpAnswerTupleList =
                    line.trimStart { char -> char != '[' }.trimEnd { char -> char != ']' }

                parseRawTPTPAnswerTupleList(tptpAnswerTupleList).fold(
                    onSuccess = {
                        parsedResult.addAll(it.first)
                    },
                    onFailure = {
                        return Result.failure(it)
                    }
                )
            }
            if (line.contains("(SZS status ContradictoryAxioms for)|(^\\s*unsat\\s*$)".toRegex(RegexOption.IGNORE_CASE))) refutationFound =
                true
        }

        if (refutationFound) {
            if (querySurface.graffiti.isEmpty()) return Result.success(
                Transformer().toNotation3Sublanguage(
                    PositiveSurface(
                        emptyList(),
                        querySurface.hayesGraph
                    )
                )
            )
            return Result.success("Refutation found")
        }

        return if (parsedResult.isEmpty()) Result.success("No answers") else
            transformQuestionAnsweringResult(
                parsedResult,
                querySurface
            )
    }

    fun transformTPTPTupleAnswerToRDFSurfaces(
        querySurface: QuerySurface,
        tptpTupleAnswer: String,
    ): Result<String> {
        return parseRawTPTPAnswerTupleList(tptpTupleAnswer).fold(
            onSuccess = {
                transformQuestionAnsweringResult(
                    it.first.toSet(),
                    querySurface
                )

            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun transformQuestionAnsweringResult(resultList: Set<List<RdfTripleElement>>, querySurface: QuerySurface) =
        runCatching { querySurface.replaceBlankNodes(resultList).let { Transformer().toNotation3Sublanguage(it) } }


    private fun parseRawTPTPAnswerTupleList(
        tptpTupleAnswer: String,
    ): Result<Pair<List<List<RdfTripleElement>>, List<List<List<RdfTripleElement>>>>> {
        return try {
            Result.success(TptpTupleAnswerFormTransformer.parseToEnd(tptpTupleAnswer))
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> Result.failure(InvalidInputException("'$tptpTupleAnswer': TPTP Answer Tuple could not be parsed. Please check the syntax.", exc))
                is InvalidInputException -> Result.failure(InvalidInputException("'$tptpTupleAnswer': ${exc.message ?: ""}", exc))
                else -> throw exc
            }
        }
    }
}