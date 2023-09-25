import parser.stringLiteralLongQuote
import parser.stringLiteralLongSingleQuote
import parser.stringLiteralQuote
import parser.stringLiteralSingleQuote
import rdfSurfaces.*
import rdfSurfaces.Collection
import util.TransformerException

class Transformer {
    fun toNotation3Sublanguage(defaultPositiveSurface: PositiveSurface): String {
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
                //TODO()
                //IRIConstants.XSD_INTEGER, IRIConstants.XSD_DOUBLE, IRIConstants.XSD_BOOLEAN_IRI, IRIConstants.XSD_DECIMAL
                IRIConstants.XSD_STRING_IRI -> {
                    val rawLiteral = literal.literalValue.toString()
                    when {
                        "\"$rawLiteral\"".matches(stringLiteralQuote) -> "\"$rawLiteral\""
                        "'$rawLiteral'".matches(stringLiteralSingleQuote) -> "'$rawLiteral'"
                        "\"\"\"$rawLiteral\"\"\"".matches(stringLiteralLongQuote) -> "\"\"\"$rawLiteral\"\"\""
                        "'''$rawLiteral'''".matches(stringLiteralLongSingleQuote) -> "'''$rawLiteral'''"
                        else -> throw TransformerException("Error during the transformation of a string literal")
                    }
                }

                else -> "\"${literal.literalValue}\"^^${transform(IRI.from(literal.datatype.uri))}"
                //TODO(${transform(IRI.fromFullString(literal.datatype.uri))})
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
        }

        fun transform(hayesGraphElement: HayesGraphElement, depth: Int): String {
            val newDepthSpace = spaceBase.repeat(depth.takeIf { it >= 0 } ?: 0)
            return when (hayesGraphElement) {
                is RDFSurface -> {
                    val surfaceNameString = when (hayesGraphElement) {
                        is PositiveSurface -> "log:onPositiveSurface"
                        is NegativeSurface -> "log:onNegativeSurface"
                        is QuerySurface -> "log:onQuerySurface"
                        is NegativeTripleSurface -> "log:negativeTriple"
                        is NeutralSurface -> "log:onNeutralSurface"
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
        defaultPositiveSurface: PositiveSurface,
        ignoreQuerySurfaces: Boolean = false,
        tptpName: String = "axiom",
        formulaRole: String = "axiom",
    ): String {

        val spaceBase = "   "
        val doubleSpaceBase = spaceBase.repeat(2)

        //TODO(make collision safe)
        fun transform(blankNode: BlankNode) = blankNode.blankNodeId.replaceFirstChar { it.uppercaseChar() }

        fun transform(iri: IRI) = "'${iri.iri}'"

        fun transform(literal: Literal) = "'" + encodeToValidTPTPLiteral(
            when (literal) {
                is LanguageTaggedString -> "\"${literal.lexicalForm}\"@${literal.languageTag}"
                else -> "\"${literal.literalValue}\"^^${literal.datatype.uri}"
            }
        ) + "'"

        fun transform(rdfTripleElement: RdfTripleElement): String = when (rdfTripleElement) {
            is BlankNode -> transform(rdfTripleElement)
            is Literal -> transform(rdfTripleElement)
            is IRI -> transform(rdfTripleElement)
            is Collection -> "list".takeIf { rdfTripleElement.list.isEmpty() }
                ?: ("list(" + rdfTripleElement.list.joinToString(",") { transform(it) } + ")")
        }

        fun transform(blankNodeList: List<BlankNode>) = blankNodeList.joinToString(separator = ",") { transform(it) }

        fun transform(hayesGraphElement: HayesGraphElement, depth: Int): String {
            val newDepthSpace = spaceBase.repeat(depth.takeIf { it >= 0 } ?: 0)
            val nextDepthSpace = newDepthSpace.plus(spaceBase)

            return when (hayesGraphElement) {
                is RDFSurface -> {
                    val fofVariableList = transform(hayesGraphElement.graffiti)
                    when (hayesGraphElement) {
                        is PositiveSurface -> "\$true".takeIf { hayesGraphElement.hayesGraph.isEmpty() }
                            ?: if (hayesGraphElement.graffiti.isEmpty()) {
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


                        is NegativeSurface, is QuerySurface, is NegativeTripleSurface -> "\$false".takeIf { hayesGraphElement.hayesGraph.isEmpty() }
                            ?: if (hayesGraphElement.graffiti.isEmpty()) {
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
                        is NeutralSurface -> throw TransformerException("Surface-type is not supported: neutral surface")

                    }
                }

                is RdfTriple -> "triple(${transform(hayesGraphElement.rdfSubject)},${transform(hayesGraphElement.rdfPredicate)},${
                    transform(
                        hayesGraphElement.rdfObject
                    )
                })"

            }
        }

        val fofVariableList = transform(defaultPositiveSurface.graffiti)

        val (querySurfaces, otherSurfaces) = defaultPositiveSurface.hayesGraph.partition { it is QuerySurface }

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
            val querySurface = surface as QuerySurface
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

    fun encodeToValidTPTPLiteral(string: String) = buildString {
        string.toCharArray().forEach { c ->
            if (isPrintableAscii(c) && c != '\\' && c != '\'') {
                this.append(c)
            } else {
                val hexValue = Integer.toHexString(c.code)
                this.append("\\\\u${hexValue.padStart(4, '0').uppercase()}")
            }
        }
    }

    fun encodeToValidTPTPVariable(string: String): String {
        return buildString {
            string.toCharArray().forEachIndexed { index, c ->
                if ((isPrintableAscii(c) && c != 'U') && (index != 0 || c.isUpperCase())) {
                    this.append(c)
                } else {
                    val hexValue = Integer.toHexString(c.code)
                    this.append("U${hexValue.padStart(4, '0').uppercase()}")
                }
            }
        }
    }

    fun decodeValidTPTPVariable(string: String) = string.replace("U[0-9A-Fa-f]{4}".toRegex()) {
        Integer.parseInt(it.value.drop(1), 16).toChar().toString()
    }

    fun decodeValidTPTPLiteral(string: String) = string.replace("\\\\\\\\u[0-9A-Fa-f]{4}".toRegex()) {
        Integer.parseInt(it.value.drop(3), 16).toChar().toString()
    }

    private fun isPrintableAscii(char: Char) = char.code in 32..127

}




