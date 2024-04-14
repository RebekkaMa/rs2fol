package controller

import parser.stringLiteralLongQuote
import parser.stringLiteralLongSingleQuote
import parser.stringLiteralQuote
import parser.stringLiteralSingleQuote
import model.*
import model.rdf_term.*
import model.rdf_term.Collection
import util.IRIConstants
import util.NotSupportedException
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
                IRIConstants.XSD_STRING_IRI -> {
                    val rawLiteral = literal.literalValue.toString()
                    when {
                        "\"$rawLiteral\"".matches(stringLiteralQuote) -> "\"$rawLiteral\""
                        "'$rawLiteral'".matches(stringLiteralSingleQuote) -> "'$rawLiteral'"
                        "\"\"\"$rawLiteral\"\"\"".matches(stringLiteralLongQuote) -> "\"\"\"$rawLiteral\"\"\""
                        "'''$rawLiteral'''".matches(stringLiteralLongSingleQuote) -> "'''$rawLiteral'''"
                        else -> throw TransformerException("Error during the transformation of a string literal to N3")
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

        fun transform(blankNode: BlankNode) = encodeToValidTPTPVariable(blankNode.blankNodeId)

        fun transform(iri: IRI) = "'${encodeToValidTPTPLiteral(iri.iri)}'"

        fun transform(literal: Literal) = "'" + encodeToValidTPTPLiteral(
            when (literal) {
                is LanguageTaggedString -> "\"${literal.lexicalForm}\"@${literal.languageTag}"
                else -> "\"${literal.literalValue}\"^^${literal.datatype.uri}"
            }
        ) + "'"

        fun transform(rdfTerm: RdfTerm): String = when (rdfTerm) {
            is BlankNode -> transform(rdfTerm)
            is Literal -> transform(rdfTerm)
            is IRI -> transform(rdfTerm)
            is Collection -> "list".takeIf { rdfTerm.list.isEmpty() }
                ?: ("list(" + rdfTerm.list.joinToString(",") { transform(it) } + ")")
        }

        fun transform(blankNodeList: List<BlankNode>) = blankNodeList.joinToString(separator = ",") { transform(it) }

        fun transform(hayesGraphElement: HayesGraphElement, depth: Int): String {
            val newDepthSpace = spaceBase.repeat(depth.takeIf { it >= 0 } ?: 0)
            val nextDepthSpace = newDepthSpace.plus(spaceBase)

            return when (hayesGraphElement) {
                is RdfSurface -> {
                    val fofVariableList = transform(hayesGraphElement.graffiti)
                    when (hayesGraphElement) {
                        is PositiveSurface -> if (hayesGraphElement.graffiti.isEmpty()) {
                                if (hayesGraphElement.hayesGraph.isEmpty()) "\$true" else
                                hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "",
                                    separator = "${System.lineSeparator()}$newDepthSpace& ",
                                    postfix = ""
                                ) { transform(it, depth + 1) }
                            } else {
                            if (hayesGraphElement.hayesGraph.isEmpty()) "? [$fofVariableList] : (${System.lineSeparator()}$nextDepthSpace\$true${System.lineSeparator()}$newDepthSpace)" else
                            hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "? [$fofVariableList] : (${System.lineSeparator()}$nextDepthSpace",
                                    separator = "${System.lineSeparator()}$nextDepthSpace& ",
                                    postfix = "${System.lineSeparator()}$newDepthSpace)"
                                ) { transform(it, depth + 1) }
                            }


                        is NegativeSurface, is QuerySurface, is NegativeTripleSurface -> if (hayesGraphElement.graffiti.isEmpty()) {
                            if (hayesGraphElement.hayesGraph.isEmpty()) "\$false" else
                            hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "~(${System.lineSeparator()}$nextDepthSpace",
                                    separator = "${System.lineSeparator()}$nextDepthSpace& ",
                                    postfix = "${System.lineSeparator()}$newDepthSpace)"
                                ) { transform(it, depth + 1) }
                            } else {
                            if (hayesGraphElement.hayesGraph.isEmpty()) "! [$fofVariableList] : ~(${System.lineSeparator()}$nextDepthSpace\$false${System.lineSeparator()}$newDepthSpace)" else
                            hayesGraphElement.hayesGraph.joinToString(
                                    prefix = "! [$fofVariableList] :  ~(${System.lineSeparator()}$nextDepthSpace",
                                    separator = "${System.lineSeparator()}$nextDepthSpace& ",
                                    postfix = "${System.lineSeparator()}$newDepthSpace)"
                                ) { transform(it, depth + 1) }
                            }

                        is NeutralSurface, is QuestionSurface, is AnswerSurface -> throw NotSupportedException("Transformation of surface type is not supported: ${hayesGraphElement.javaClass.name}")

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

        val (querySurfaces, questionSurface, otherHayesGraphElements) = defaultPositiveSurface.hayesGraph.fold(
            Triple<List<QuerySurface>, List<QuestionSurface>, List<HayesGraphElement>>(
                listOf(), listOf(), listOf()
            )
        ) { acc, hayesGraphElement ->
            when (hayesGraphElement) {
                is QuerySurface -> acc.copy(first = acc.first.plus(hayesGraphElement))
                is QuestionSurface -> acc.copy(second = acc.second.plus(hayesGraphElement))
                else -> acc.copy(third = acc.third.plus(hayesGraphElement))
            }
        }

        val fofQuantifiedFormula = otherHayesGraphElements.let {
            val fofFormula = if (it.isEmpty()) "\$true" else {
                if (defaultPositiveSurface.graffiti.isEmpty()) {
                    otherHayesGraphElements.joinToString(
                        prefix = "",
                        separator = "${System.lineSeparator()}$spaceBase& ",
                        postfix = ""
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                } else {
                    otherHayesGraphElements.joinToString(
                        prefix = "? [$fofVariableList] : (${System.lineSeparator()}$doubleSpaceBase",
                        separator = "${System.lineSeparator()}$doubleSpaceBase& ",
                        postfix = "${System.lineSeparator()}$spaceBase)"
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                }
            }
            "fof($tptpName,$formulaRole,${System.lineSeparator()}$spaceBase$fofFormula${System.lineSeparator()})."
        }

        val fofQuantifiedFormulaQuery = querySurfaces.mapIndexed { index, surface ->
            val fofFormula =
                if (surface.graffiti.isEmpty()) {
                    if (surface.hayesGraph.isEmpty()) "\$true" else
                        surface.hayesGraph.joinToString(
                            prefix = "",
                            separator = "${System.lineSeparator()}$spaceBase& ",
                            postfix = ""
                        ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                } else {
                    if (surface.hayesGraph.isEmpty()) "? [${transform(surface.graffiti)}] : (${System.lineSeparator()}$doubleSpaceBase\$true${System.lineSeparator()}$spaceBase)" else
                        surface.hayesGraph.joinToString(
                            prefix = "? [${transform(surface.graffiti)}] : (${System.lineSeparator()}$doubleSpaceBase",
                            separator = "${System.lineSeparator()}$doubleSpaceBase& ",
                            postfix = "${System.lineSeparator()}$spaceBase)"
                        ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                }

            "fof(query_$index,question,${System.lineSeparator()}$spaceBase$fofFormula${System.lineSeparator()})."
        }

        val fofQuantifiedFormulaQuestion = questionSurface.mapIndexed { index, surface ->
            val hayesGraphElementsWithoutAnswerSurface = surface.hayesGraph.filterNot { it is AnswerSurface }
            val fofFormula =
                if (surface.graffiti.isEmpty()) {
                    if (hayesGraphElementsWithoutAnswerSurface.isEmpty()) "\$true" else
                    hayesGraphElementsWithoutAnswerSurface.joinToString(
                        prefix = "",
                        separator = "${System.lineSeparator()}$spaceBase& ",
                        postfix = ""
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                } else {
                    if (hayesGraphElementsWithoutAnswerSurface.isEmpty()) "? [${transform(surface.graffiti)}] : (${System.lineSeparator()}$doubleSpaceBase\$true${System.lineSeparator()}$spaceBase)" else
                    hayesGraphElementsWithoutAnswerSurface.joinToString(
                        prefix = "? [${transform(surface.graffiti)}] : (${System.lineSeparator()}$doubleSpaceBase",
                        separator = "${System.lineSeparator()}$doubleSpaceBase& ",
                        postfix = "${System.lineSeparator()}$spaceBase)"
                    ) { hayesGraphElement -> transform(hayesGraphElement, 2) }
                }

            "fof(question_$index,question,${System.lineSeparator()}$spaceBase$fofFormula${System.lineSeparator()})."
        }

        return buildString {
            this.append(fofQuantifiedFormula)
            if (ignoreQuerySurfaces) return@buildString

            fofQuantifiedFormulaQuery.joinToString(
                prefix = System.lineSeparator(),
                separator = System.lineSeparator()
            ).takeUnless { fofQuantifiedFormulaQuery.isEmpty() }?.let { append(it) }

            fofQuantifiedFormulaQuestion.joinToString(
                prefix = System.lineSeparator(),
                separator = System.lineSeparator()
            ).takeUnless { fofQuantifiedFormulaQuestion.isEmpty() }?.let { append(it) }
        }
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
            val hexValueForO = Integer.toHexString('O'.code).uppercase().padStart(4, '0')
            val hexValueForx = Integer.toHexString('x'.code).uppercase().padStart(4, '0')
            val strWithoutOx = string.replace("Ox([0-9A-Fa-f]{4})".toRegex()) {
                "Ox${hexValueForO}Ox$hexValueForx" + it.destructured.component1()
            }
            strWithoutOx.toCharArray().forEachIndexed { index, c ->
                if ((isPrintableAscii(c)) && (index != 0 || c.isUpperCase())) {
                    this.append(c)
                } else {
                    val hexValue = Integer.toHexString(c.code).uppercase().padStart(4, '0')
                    this.append("Ox$hexValue")
                }
            }
        }
    }

    fun decodeValidTPTPVariable(string: String) = string.replace("Ox[0-9A-Fa-f]{4}".toRegex()) {
        Integer.parseInt(it.value.drop(2), 16).toChar().toString()
    }

    fun decodeValidTPTPLiteral(string: String) = string.replace("\\\\\\\\u[0-9A-Fa-f]{4}".toRegex()) {
        Integer.parseInt(it.value.drop(3), 16).toChar().toString()
    }

    private fun isPrintableAscii(char: Char) = char.code in 32..127

}




