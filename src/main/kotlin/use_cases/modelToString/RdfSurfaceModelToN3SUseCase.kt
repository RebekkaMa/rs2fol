package use_cases.modelToString

import entities.rdfsurfaces.*
import entities.rdfsurfaces.rdf_term.*
import entities.rdfsurfaces.rdf_term.Collection
import interface_adapters.services.coder.N3SRDFTermCoderService
import interface_adapters.services.parser.util.stringLiteralLongQuote
import interface_adapters.services.parser.util.stringLiteralLongSingleQuote
import interface_adapters.services.parser.util.stringLiteralQuote
import interface_adapters.services.parser.util.stringLiteralSingleQuote
import util.IRIConstants
import util.LiteralTransformationException
import util.commandResult.Error
import util.commandResult.IntermediateStatus

object RdfSurfaceModelToN3UseCase {

    operator fun invoke(
        defaultPositiveSurface: PositiveSurface,
        dEntailment: Boolean = false
    ): IntermediateStatus<String, LiteralTransformationError> {
        val spaceBase = "   "

        var prefixCounter = 0
        val prefixMap = mutableMapOf<String, String>()

        fun transform(blankNode: BlankNode): String {
            val blankNodeId =
                if (N3SRDFTermCoderService.isValid(blankNode)) blankNode.blankNodeId else N3SRDFTermCoderService.encode(
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
                        else -> throw LiteralTransformationException(literal = "Value: $rawLiteral, URI: ${literal.datatypeIRI.iri}")
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
            return IntermediateStatus.Error(LiteralTransformationError(exception.literal))
        }

        return IntermediateStatus.Result(rdfSurfacesGraphString.toString())
    }
}


data class LiteralTransformationError(val literal: String) : Error