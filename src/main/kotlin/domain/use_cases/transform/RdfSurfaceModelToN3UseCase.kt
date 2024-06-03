package domain.use_cases.transform

import domain.entities.*
import domain.entities.rdf_term.*
import domain.entities.rdf_term.Collection
import domain.error.Error
import domain.error.Result
import interface_adapters.services.parsing.util.stringLiteralLongQuote
import interface_adapters.services.parsing.util.stringLiteralLongSingleQuote
import interface_adapters.services.parsing.util.stringLiteralQuote
import interface_adapters.services.parsing.util.stringLiteralSingleQuote
import util.IRIConstants
import util.LiteralTransformationException
import util.TransformerException

object RdfSurfaceModelToN3UseCase {

    operator fun invoke(
        defaultPositiveSurface: PositiveSurface
    ) : Result<String, LiteralTransformationError> {
        val spaceBase = "   "

        var prefixCounter = 0
        val prefixMap = mutableMapOf<String, String>()

        fun transform(blankNode: BlankNode) = "_:${blankNode.blankNodeId}"

        fun transform(iri: IRI) = when (val iriWithoutFragment = iri.getIRIWithoutFragment()) {
            IRIConstants.LOG_IRI -> {
                prefixMap.putIfAbsent(IRIConstants.LOG_IRI, "log")
                "log:${iri.fragment.orEmpty()}"
            }

            IRIConstants.RDF_IRI -> {
                prefixMap.putIfAbsent(IRIConstants.RDF_IRI, "rdf")
                "rdf:${iri.fragment.orEmpty()}"
            }

            IRIConstants.XSD_IRI -> {
                prefixMap.putIfAbsent(IRIConstants.XSD_IRI, "xsd")
                "xsd:${iri.fragment.orEmpty()}"
            }

            IRIConstants.RDFS_IRI -> {
                prefixMap.putIfAbsent(IRIConstants.RDFS_IRI, "rdfs")
                "rdfs:${iri.fragment.orEmpty()}"
            }

            else -> {
                val fragment = iri.fragment
                if (fragment.isNullOrBlank()) "<${iri.iri}>" else {
                    val prefix = prefixMap.getOrPut(iriWithoutFragment) {
                        when (prefixCounter++) {
                            0 -> ""
                            1 -> "ex"
                            else -> "ex$prefixCounter"
                        }
                    }
                    "$prefix:${fragment}"
                }

            }

        }

        fun transform(literal: Literal): String {
            if (literal is LanguageTaggedString) return "\"${literal.lexicalForm}\"@${literal.languageTag}"
            return when (literal.datatype.uri) {
                IRIConstants.XSD_STRING_IRI -> {
                    val rawLiteral = literal.literalValue.toString()
                    when {
                        "\"$rawLiteral\"".matches(stringLiteralQuote) -> "\"$rawLiteral\""
                        "'$rawLiteral'".matches(stringLiteralSingleQuote) -> "'$rawLiteral'"
                        "\"\"\"$rawLiteral\"\"\"".matches(stringLiteralLongQuote) -> "\"\"\"$rawLiteral\"\"\""
                        "'''$rawLiteral'''".matches(stringLiteralLongSingleQuote) -> "'''$rawLiteral'''"
                        else -> throw LiteralTransformationException(literal = "Value: $rawLiteral, URI: ${literal.datatype.uri}")
                    }
                }

                else -> "\"${literal.literalValue}\"^^${transform(IRI.from(literal.datatype.uri))}"
            }
        }


        fun transform(graffitiList: List<BlankNode>) = graffitiList.joinToString(
            prefix = "(",
            postfix = ")",
            separator = " "
        ) { transform(it) }

        fun transform(rdfTerm: RdfTerm): String = when (rdfTerm) {
            is BlankNode -> transform(rdfTerm)
            is Literal -> transform(rdfTerm)
            is IRI -> transform(rdfTerm)
            is Collection -> rdfTerm.list.joinToString(
                prefix = "(",
                separator = " ",
                postfix = ")"
            ) { transform(it) }
        }

        fun transform(hayesGraphElement: HayesGraphElement, depth: Int): String {
            val newDepthSpace = spaceBase.repeat(depth.takeIf { it >= 0 } ?: 0)
            return when (hayesGraphElement) {
                is RdfSurface -> {
                    val surfaceNameString = when (hayesGraphElement) {
                        is PositiveSurface -> "log:onPositiveSurface"
                        is NegativeSurface -> "log:onNegativeSurface"
                        is QuerySurface -> "log:onQuerySurface"
                        is NegativeTripleSurface -> "log:negativeTriple"
                        is NeutralSurface -> "log:onNeutralSurface"
                        is QuestionSurface -> "log:onQuestionSurface"
                        is AnswerSurface -> "log:onAnswerSurface"
                    }
                    val graffitiStringList = transform(hayesGraphElement.graffiti)
                    val hayesGraphString =
                        hayesGraphElement.hayesGraph.joinToString(
                            prefix = System.lineSeparator(),
                            separator = System.lineSeparator(),
                            postfix = System.lineSeparator()
                        ) {
                            transform(
                                hayesGraphElement = it,
                                depth + 1
                            )
                        }
                    prefixMap.putIfAbsent(IRIConstants.LOG_IRI, "log")
                    "$newDepthSpace$graffitiStringList $surfaceNameString {$hayesGraphString$newDepthSpace}."
                }

                is RdfTriple -> "$newDepthSpace${transform(hayesGraphElement.rdfSubject)} ${
                    transform(
                        hayesGraphElement.rdfPredicate
                    )
                } ${transform(hayesGraphElement.rdfObject)}."
            }
        }

        val rdfSurfacesGraphString = StringBuilder()

        try {
            val graffitiListString = transform(defaultPositiveSurface.graffiti)
            val hayesGraphString =
                defaultPositiveSurface.hayesGraph.joinToString(
                    prefix = System.lineSeparator(),
                    separator = System.lineSeparator(),
                    postfix = System.lineSeparator()
                ) {
                    transform(
                        hayesGraphElement = it,
                        (0).takeIf { defaultPositiveSurface.graffiti.isEmpty() } ?: 1)
                }

            if (defaultPositiveSurface.graffiti.isNotEmpty()) rdfSurfacesGraphString.append("$graffitiListString log:onPositiveSurface {$hayesGraphString}.") else rdfSurfacesGraphString.append(
                hayesGraphString
            )

            prefixMap.forEach { (iri, prefix) ->
                rdfSurfacesGraphString.insert(0, "@prefix $prefix: <$iri>.${System.lineSeparator()}")
            }
        } catch (exception : LiteralTransformationException) {
            return Result.Error(LiteralTransformationError(exception.literal))
        }

        return Result.Success(rdfSurfacesGraphString.toString())
    }
}


data class LiteralTransformationError(val literal: String) : Error