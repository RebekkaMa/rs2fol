import rdfSurface.*
import rdfSurface.Collection

class TransformerException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class Transformer {
    fun printRDFSurfaceGraphUsingNotation3(defaultPositiveSurface: PositiveRDFSurface): String {
        val spaceBase = "   "

        fun transform(blankNode: BlankNode) = "_:${blankNode.blankNodeId}"

        fun transform(iri: IRI) = when {
            iri.iri.startsWith(IRIConstants.LOG_IRI) -> "log:${iri.iri.removePrefix(IRIConstants.LOG_IRI)}"
            iri.iri.startsWith(IRIConstants.RDF_IRI) -> "rdf:${iri.iri.removePrefix(IRIConstants.RDF_IRI)}"
            else -> "<${iri.iri}>"
        }

        fun transform(literal: Literal) =
            when (literal.xsdDatatype.uri) {
                IRIConstants.RDF_LANG_STRING_IRI -> {
                    val (lexVal, langTag) = literal.lexicalValue as Pair<*, *>
                    "\"$lexVal\"$langTag"
                }
                IRIConstants.XSD_INTEGER,IRIConstants.XSD_DOUBLE, IRIConstants.XSD_BOOLEAN_IRI -> literal.lexicalValue.toString()
                else -> "\"${literal.lexicalValue}\"^^<${literal.xsdDatatype.uri}>"
            }

        fun transform(graffitiList: List<BlankNode>) = graffitiList.joinToString(
            prefix = "(",
            postfix = ")",
            separator = " "
        ) { transform(it) }

        fun transform(rdfTripleElement: RdfTripleElement) : String = when (rdfTripleElement) {
            is BlankNode -> transform(rdfTripleElement)
            is Literal -> transform(rdfTripleElement)
            is IRI -> transform(rdfTripleElement)
            is Collection -> rdfTripleElement.list.joinToString(prefix = "(", separator = " ", postfix = ")"){transform(it)}
            else -> throw TransformerException("RDF triple element type not supported")
        }

        fun transform(hayesGraphElement: HayesGraphElement, depth: Int): String {
            val newDepthSpace = spaceBase.repeat(depth.takeIf { it >= 0 } ?: 0)
            return when (hayesGraphElement) {
                is RDFSurface -> {
                    val surfaceNameString = when (hayesGraphElement) {
                        is PositiveRDFSurface -> "log:onPositiveSurface"
                        is NegativeRDFSurface -> "log:onNegativeSurface"
                        is QueryRDFSurface -> "log:onQuerySurface"
                        is NegativeTripleRDFSurface -> "log:negativeTriple"
                        is NeutralRDFSurface -> "log:onNeutralSurface"
                        else -> throw TransformerException("Surface-type is not supported")
                    }
                    val graffitiStringList = transform(hayesGraphElement.graffiti)
                    val hayesGraphString =
                        hayesGraphElement.hayesGraph.joinToString(
                            prefix = "\n",
                            separator = "\n",
                            postfix = "\n"
                        ) {
                            transform(
                                hayesGraphElement = it,
                                depth + 1
                            )
                        }
                    "$newDepthSpace$graffitiStringList $surfaceNameString {$hayesGraphString$newDepthSpace}."
                }

                is RdfTriple -> "$newDepthSpace${transform(hayesGraphElement.rdfSubject)} ${
                    transform(
                        hayesGraphElement.rdfPredicate
                    )
                } ${transform(hayesGraphElement.rdfObject)}."

                else -> throw TransformerException("HayesGraphElement type not supported")
            }
        }

        val rdfSurfacesGraphString = StringBuilder()
        rdfSurfacesGraphString.append("@prefix log: <${IRIConstants.LOG_IRI}>.\n")
        rdfSurfacesGraphString.append("@prefix rdf: <${IRIConstants.RDF_IRI}>.\n\n")

        val graffitiListString = transform(defaultPositiveSurface.graffiti)
        val hayesGraphString =
            defaultPositiveSurface.hayesGraph.joinToString(
                prefix = "\n",
                separator = "\n",
                postfix = "\n"
            ) {
                transform(
                    hayesGraphElement = it,
                    (0).takeIf { defaultPositiveSurface.graffiti.isEmpty() } ?: 1)
            }
        if (defaultPositiveSurface.graffiti.isNotEmpty()) rdfSurfacesGraphString.append("$graffitiListString log:onPositiveSurface {$hayesGraphString}.") else rdfSurfacesGraphString.append(
            hayesGraphString
        )
        return rdfSurfacesGraphString.toString()
    }

    fun transformToFOL(
        defaultPositiveSurface: PositiveRDFSurface,
        ignoreQuerySurfaces: Boolean = false,
        tptpName: String = "axiom",
        formulaRole: String = "axiom"
    ): String {

        fun transform(blankNode: BlankNode) = blankNode.blankNodeId.replaceFirstChar { it.uppercaseChar() }

        fun transform(iri: IRI) = "'${iri.iri}'"

        fun transform(literal: Literal) = "'" + when (literal.xsdDatatype.uri) {
            IRIConstants.RDF_LANG_STRING_IRI -> {
                val (lexVal, langTag) = literal.lexicalValue as Pair<*, *>
                "\"" + lexVal + "\"" + langTag
            }

            else -> "\"${literal.lexicalValue}\"^^${literal.xsdDatatype.uri}"
        } + "'"

        fun transform(rdfTripleElement: RdfTripleElement) : String = when (rdfTripleElement) {
            is BlankNode -> transform(rdfTripleElement)
            is Literal -> transform(rdfTripleElement)
            is IRI -> transform(rdfTripleElement)
            is Collection -> if (rdfTripleElement.list.isEmpty()) "list" else ("list(" + rdfTripleElement.list.joinToString(",") {transform(it)} + ")")
            else -> throw TransformerException("RDF triple element type not supported")
        }

        fun transform(blankNodeList: List<BlankNode>) = blankNodeList.joinToString(separator = ",") { transform(it) }

        fun transform(hayesGraphElement: HayesGraphElement): String {
            return when (hayesGraphElement) {
                is RDFSurface -> {
                    val fofVariableList = transform(hayesGraphElement.graffiti)
                    when (hayesGraphElement) {
                        is PositiveRDFSurface -> if (hayesGraphElement.hayesGraph.isEmpty()) "\$true" else hayesGraphElement.hayesGraph.joinToString(
                            prefix = (if (hayesGraphElement.graffiti.isEmpty()) "" else "(? [$fofVariableList] : ") + "(",
                            separator = " & ",
                            postfix = (if (hayesGraphElement.graffiti.isEmpty()) "" else ")") + ")"
                        ) { transform(it) }

                        is NegativeRDFSurface, is QueryRDFSurface, is NegativeTripleRDFSurface -> if (hayesGraphElement.hayesGraph.isEmpty()) "\$false" else hayesGraphElement.hayesGraph.joinToString(
                            prefix = (if (hayesGraphElement.graffiti.isEmpty()) "" else "! [$fofVariableList] : ") + "~(",
                            separator = " & ",
                            postfix = ")"
                        ) { transform(it) }

                        else -> throw TransformerException("Surface-type is not supported")
                    }
                }

                is RdfTriple -> "triple(" + transform(hayesGraphElement.rdfSubject) + "," + transform(hayesGraphElement.rdfPredicate) + "," + transform(
                    hayesGraphElement.rdfObject
                ) + ")"

                else -> throw TransformerException("HayesGraphElement type not supported")
            }
        }

        val fofVariableList = transform(defaultPositiveSurface.graffiti)

        val (querySurfaces, otherSurfaces) = defaultPositiveSurface.hayesGraph.partition { it is QueryRDFSurface }

        val fofQuantifiedFormula = otherSurfaces.let {
            val fofFormula = if (it.isEmpty()) "\$true" else otherSurfaces.joinToString(
                prefix = (if (defaultPositiveSurface.graffiti.isEmpty()) "" else " ? [$fofVariableList] : ") + "(",
                separator = " & ",
                postfix = ")"
            ) { hayesGraphElement -> transform(hayesGraphElement) }
            "fof($tptpName,$formulaRole,$fofFormula)."
        }

        val fofQuantifiedFormulaQuestion = querySurfaces.mapIndexed { index, surface ->
            val querySurface = surface as QueryRDFSurface
            val fofFormula = if (querySurface.hayesGraph.isEmpty()) "\$true" else querySurface.hayesGraph.joinToString(
                prefix = (if (querySurface.graffiti.isEmpty()) "" else "? [${transform(querySurface.graffiti)}] : ") + "(",
                separator = " & ",
                postfix = ")"
            ) { transform(it) }
            "fof(question$index,question,$fofFormula)."
        }

        return fofQuantifiedFormula + if (!ignoreQuerySurfaces && fofQuantifiedFormulaQuestion.isNotEmpty()) fofQuantifiedFormulaQuestion.joinToString(
            prefix = "\n",
            separator = "\n"
        ) else ""
    }

    fun transformQuestionAnsweringResultToRDFSurfaces(){

    }
}