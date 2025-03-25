package app.use_cases.modelToString

import adapter.coder.N3SRDFTermCoderService
import adapter.parser.util.stringLiteralLongQuote
import adapter.parser.util.stringLiteralLongSingleQuote
import adapter.parser.util.stringLiteralQuote
import adapter.parser.util.stringLiteralSingleQuote
import app.use_cases.results.modelToString.RdfSurfaceModelToN3sResult
import entities.rdfsurfaces.*
import entities.rdfsurfaces.rdf_term.*
import entities.rdfsurfaces.rdf_term.Collection
import util.IRIConstants
import util.commandResult.Result

class RdfSurfaceModelToN3UseCase(
    private val n3SRDFTermCoderService: N3SRDFTermCoderService
) {

    operator fun invoke(
        defaultPositiveSurface: PositiveSurface,
        dEntailment: Boolean = false
    ): Result<String, RdfSurfaceModelToN3sResult.Error> {
        val spaceBase = "   "

        var prefixCounter = 0
        val prefixMap = mutableMapOf<String, String>()

        fun transform(blankNode: BlankNode): String {
            val blankNodeId =
                if (n3SRDFTermCoderService.isValid(blankNode)) blankNode.blankNodeId else n3SRDFTermCoderService.encode(
                    blankNode
                ).blankNodeId
            return "_:${blankNodeId}"
        }

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
            if (literal is LanguageTaggedString) {
                if (dEntailment) return "\"${literal.lexicalValue}\"@${literal.normalizedLangTag}"
                return "\"${literal.lexicalValue}\"@${literal.langTag}"
            }
            return when (literal.datatypeIRI.iri) {
                IRIConstants.XSD_STRING_IRI -> {
                    val rawLiteral = literal.lexicalValue
                    when {
                        "\"$rawLiteral\"".matches(stringLiteralQuote) -> "\"$rawLiteral\""
                        "'$rawLiteral'".matches(stringLiteralSingleQuote) -> "'$rawLiteral'"
                        "\"\"\"$rawLiteral\"\"\"".matches(stringLiteralLongQuote) -> "\"\"\"$rawLiteral\"\"\""
                        "'''$rawLiteral'''".matches(stringLiteralLongSingleQuote) -> "'''$rawLiteral'''"
                        else -> throw LiteralTransformationException(value = rawLiteral, iri = literal.datatypeIRI.iri)
                    }
                }

                else -> {
                    if (dEntailment) return "\"${literal.literalValue}\"^^${transform(literal.datatypeIRI)}"
                    "\"${literal.lexicalValue}\"^^${transform(literal.datatypeIRI)}"
                }
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
                        is NeutralSurface -> "log:onNeutralSurface"
                        is NegativeAnswerSurface -> "log:onNegativeAnswerSurface"
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
                        defaultPositiveSurface.graffiti.size.coerceAtMost(1)
                    )
                }

            if (defaultPositiveSurface.graffiti.isNotEmpty()) rdfSurfacesGraphString.append("$graffitiListString log:onPositiveSurface {$hayesGraphString}.") else rdfSurfacesGraphString.append(
                hayesGraphString
            )

            prefixMap.forEach { (iri, prefix) ->
                rdfSurfacesGraphString.insert(0, "@prefix $prefix: <$iri>.${System.lineSeparator()}")
            }
        } catch (exception: LiteralTransformationException) {
            return Result.Error(RdfSurfaceModelToN3sResult.Error.LiteralTransformationError(exception.value, exception.iri))
        }

        return Result.Success(rdfSurfacesGraphString.toString())
    }
}

private class LiteralTransformationException(val value: String,val iri: String) : Exception()