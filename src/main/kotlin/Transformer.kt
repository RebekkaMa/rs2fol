import parser.stringLiteralLongQuote
import parser.stringLiteralLongSingleQuote
import parser.stringLiteralQuote
import parser.stringLiteralSingleQuote
import rdfSurfaces.*
import rdfSurfaces.Collection

class TransformerException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class Transformer {
    fun toNotation3Sublanguage(defaultPositiveSurface: PositiveRDFSurface): String {
        val spaceBase = "   "

        var prefixCounter = -1
        val prefixMap = mutableMapOf<String, String>()

        fun transform(blankNode: BlankNode) = "_:${blankNode.blankNodeId}"

        fun transform(iri: IRI) = when (val iriWithoutFragment = iri.getIRIWithoutFragment()) {
            IRIConstants.LOG_IRI -> {
                prefixMap.putIfAbsent(IRIConstants.LOG_IRI, "log")
                "log:${iri.getFragment() ?: ""}"
            }

            IRIConstants.RDF_IRI -> {
                prefixMap.putIfAbsent(IRIConstants.RDF_IRI, "rdf")
                "rdf:${iri.getFragment() ?: ""}"
            }

            IRIConstants.XSD_IRI -> {
                prefixMap.putIfAbsent(IRIConstants.XSD_IRI, "xsd")
                "xsd:${iri.getFragment() ?: ""}"
            }

            IRIConstants.RDFS_IRI -> {
                prefixMap.putIfAbsent(IRIConstants.RDF_IRI, "rdfs")
                "rdfs:${iri.getFragment() ?: ""}"
            }

            else -> {
                val fragment = iri.getFragment()
                if (fragment == null) "<${iri.iri}>" else {
                    val prefix = prefixMap.getOrPut(iriWithoutFragment) {
                        when (++prefixCounter) {
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
                //TODO()
                IRIConstants.XSD_INTEGER, IRIConstants.XSD_DOUBLE, IRIConstants.XSD_BOOLEAN_IRI -> literal.literalValue.toString()
                IRIConstants.XSD_STRING_IRI -> {
                    val rawLiteral = literal.literalValue.toString()
                    when {
                        "\"$rawLiteral\"".matches(stringLiteralQuote) -> "\"$rawLiteral\""
                        "'$rawLiteral'".matches(stringLiteralSingleQuote) -> "'$rawLiteral'"
                        "\"\"\"$rawLiteral\"\"\"".matches(stringLiteralLongQuote) -> "\"\"\"$rawLiteral\"\"\""
                        "'''$rawLiteral'''".matches(stringLiteralLongSingleQuote) -> "'''$rawLiteral'''"
                        else -> throw TransformerException("Transforming string error")
                    }
                }

                else -> "\"${literal.literalValue}\"^^${transform(IRI(literal.datatype.uri))}"
                //TODO(${transform(IRI(literal.datatype.uri))})
            }
        }


        fun transform(graffitiList: List<BlankNode>) = graffitiList.joinToString(
            prefix = "(",
            postfix = ")",
            separator = " "
        ) { transform(it) }

        fun transform(rdfTripleElement: RdfTripleElement): String = when (rdfTripleElement) {
            is BlankNode -> transform(rdfTripleElement)
            is Literal -> transform(rdfTripleElement)
            is IRI -> transform(rdfTripleElement)
            is Collection -> rdfTripleElement.list.joinToString(
                prefix = "(",
                separator = " ",
                postfix = ")"
            ) { transform(it) }

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
                    prefixMap.putIfAbsent(IRIConstants.LOG_IRI, "log")
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

        prefixMap.forEach { (iri, prefix) ->
            rdfSurfacesGraphString.insert(0, "@prefix $prefix: <$iri>.\n")
        }
        return rdfSurfacesGraphString.toString()
    }

    fun toFOL(
        defaultPositiveSurface: PositiveRDFSurface,
        ignoreQuerySurfaces: Boolean = false,
        tptpName: String = "axiom",
        formulaRole: String = "axiom"
    ): String {

        val spaceBase = "   "
        val doubleSpaceBase = spaceBase.repeat(2)

        fun transform(blankNode: BlankNode) = blankNode.blankNodeId.replaceFirstChar { it.uppercaseChar() }

        fun transform(iri: IRI) = "'${iri.iri}'"

        fun transform(literal: Literal) = "'" + when (literal) {
            is LanguageTaggedString -> "\"${encode(literal.lexicalForm)}\"@${literal.languageTag}"
            else -> "\"${encode(literal.literalValue.toString())}\"^^${literal.datatype.uri}"
        } + "'"

        fun transform(rdfTripleElement: RdfTripleElement): String = when (rdfTripleElement) {
            is BlankNode -> transform(rdfTripleElement)
            is Literal -> transform(rdfTripleElement)
            is IRI -> transform(rdfTripleElement)
            is Collection -> if (rdfTripleElement.list.isEmpty()) "list" else ("list(" + rdfTripleElement.list.joinToString(
                ","
            ) { transform(it) } + ")")

            else -> throw TransformerException("RDF triple element type not supported")
        }

        fun transform(blankNodeList: List<BlankNode>) = blankNodeList.joinToString(separator = ",") { transform(it) }

        fun transform(hayesGraphElement: HayesGraphElement, depth: Int): String {
            val newDepthSpace = spaceBase.repeat(depth.takeIf { it >= 0 } ?: 0)
            val nextDepthSpace = newDepthSpace.plus(spaceBase)

            return when (hayesGraphElement) {
                is RDFSurface -> {
                    val fofVariableList = transform(hayesGraphElement.graffiti)
                    when (hayesGraphElement) {
                        is PositiveRDFSurface -> if (hayesGraphElement.hayesGraph.isEmpty()) "\$true"
                        else {
                            if (hayesGraphElement.graffiti.isEmpty()) {
                                hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "",
                                    separator = "\n$newDepthSpace& ",
                                    postfix = ""
                                ) { transform(it, depth + 1) }
                            } else {
                                hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "? [$fofVariableList] : (\n$nextDepthSpace",
                                    separator = "\n$nextDepthSpace& ",
                                    postfix = "\n$newDepthSpace)"
                                ) { transform(it, depth + 1) }
                            }
                        }

                        is NegativeRDFSurface, is QueryRDFSurface, is NegativeTripleRDFSurface -> if (hayesGraphElement.hayesGraph.isEmpty()) "\$false" else
                            if (hayesGraphElement.graffiti.isEmpty()) {
                                hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "~(\n$nextDepthSpace",
                                    separator = "\n$nextDepthSpace& ",
                                    postfix = "\n$newDepthSpace)"
                                ) { transform(it, depth + 1) }
                            } else {
                                hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "! [$fofVariableList] :  ~(\n$nextDepthSpace",
                                    separator = "\n$nextDepthSpace& ",
                                    postfix = "\n$newDepthSpace)"
                                ) { transform(it, depth + 1) }
                            }

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
            val fofFormula = if (it.isEmpty()) "\$true" else {
                if (defaultPositiveSurface.graffiti.isEmpty()) {
                    otherSurfaces.joinToString(
                        prefix = "",
                        separator = "\n$spaceBase& ",
                        postfix = ""
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                } else {
                    otherSurfaces.joinToString(
                        prefix = "? [$fofVariableList] : (\n$doubleSpaceBase",
                        separator = "\n$doubleSpaceBase& ",
                        postfix = "\n$spaceBase)"
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                }
            }
            "fof($tptpName,$formulaRole,\n$spaceBase$fofFormula\n)."
        }

        val fofQuantifiedFormulaQuestion = querySurfaces.mapIndexed { index, surface ->
            val querySurface = surface as QueryRDFSurface
            val fofFormula = if (querySurface.hayesGraph.isEmpty()) "\$true" else {
                if (querySurface.graffiti.isEmpty()) {
                    querySurface.hayesGraph.joinToString(
                        prefix = "",
                        separator = "\n$spaceBase& ",
                        postfix = ""
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                } else {
                    querySurface.hayesGraph.joinToString(
                        prefix = "? [${transform(querySurface.graffiti)}] : (\n$doubleSpaceBase",
                        separator = "\n$doubleSpaceBase& ",
                        postfix = "\n$spaceBase)"
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                }
            }
            "fof(question_$index,question,\n$spaceBase$fofFormula\n)."
        }

        return fofQuantifiedFormula + if (!ignoreQuerySurfaces && fofQuantifiedFormulaQuestion.isNotEmpty()) fofQuantifiedFormulaQuestion.joinToString(
            prefix = "\n",
            separator = "\n"
        ) else ""
    }

    fun encode(string: String) = buildString {
        string.codePoints().forEach {
            if (isPrintableAscii(it)) {
                this.append(it.toChar())

            } else {
                val hexValue = Integer.toHexString(it)
                if (hexValue.length <= 4) this.append("\\\\u${hexValue.padStart(4, '0').toUpperCase()};") else
                    this.append("\\\\U${hexValue.padStart(8, '0').toUpperCase()};")
            }
        }
    }

    fun decode(string: String) = string.replace("\\\\\\\\[Uu][0-9A-Fa-f]+;".toRegex()) {
        Integer.parseInt(it.value.drop(3).removeSuffix(";"),16).toChar().toString()
    }

    fun isPrintableAscii(char: Int) = char in 32..127 && char != 39 && char != 92 && char != ';'.code

}




