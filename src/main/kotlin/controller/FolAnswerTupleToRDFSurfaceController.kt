package controller
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import parser.RDFSurfacesParser
import parser.TptpTupleAnswerFormTransformer
import rdfSurfaces.*
import rdfSurfaces.rdfTerm.IRI
import rdfSurfaces.rdfTerm.RdfTerm
import util.InvalidInputException
import util.NotSupportedException
import util.generalParseErrorString

class FolAnswerTupleToRDFSurfaceController {

    fun getQuerySurfaceFromRdfSurface(
        rdfSurfacesGraph: String,
        baseIRI: IRI,
        rdfLists: Boolean,
    ): Result<List<QSurface>> {
        return try {
            Result.success(RDFSurfacesParser(rdfLists).parseToEnd(rdfSurfacesGraph, baseIRI).getQSurfaces())
        } catch (exc: Exception) {
            when (exc) {
                is ParseException -> Result.failure(InvalidInputException(generalParseErrorString, exc))
                is InvalidInputException, is NotSupportedException -> Result.failure(exc)
                else -> throw exc
            }
        }
    }

    fun questionAnsweringOutputToRDFSurfacesCasc(
        qSurface: QSurface,
        questionAnsweringOutputLines: Sequence<String>,
    ): Result<String> {
        val parsedResult = mutableSetOf<List<RdfTerm>>()

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
            if (qSurface.graffiti.isEmpty()) return transformQuestionAnsweringResult(setOf(listOf()), qSurface)
            return Result.success("Refutation found")
        }

        return if (parsedResult.isEmpty()) Result.success("No answers found") else
            transformQuestionAnsweringResult(
                parsedResult,
                qSurface
            )
    }

    fun transformTPTPTupleAnswerToRDFSurfaces(
        qSurface: QSurface,
        tptpTupleAnswer: String,
    ): Result<String> {
        return parseRawTPTPAnswerTupleList(tptpTupleAnswer).fold(
            onSuccess = {
                transformQuestionAnsweringResult(
                    it.first.toSet(),
                    qSurface
                )

            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun transformQuestionAnsweringResult(resultList: Set<List<RdfTerm>>, qSurface: QSurface) =
        runCatching { qSurface.replaceBlankNodes(resultList).let { Transformer().toNotation3Sublanguage(it) } }


    private fun parseRawTPTPAnswerTupleList(
        tptpTupleAnswer: String,
    ): Result<Pair<List<List<RdfTerm>>, List<List<List<RdfTerm>>>>> {
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