package interface_adapters.services.coder

import entities.rdfsurfaces.rdf_term.*
import interface_adapters.services.parser.util.pnChars
import interface_adapters.services.parser.util.pnCharsU

object RDFTermCoderService {

    fun isValid(blankNode: BlankNode): Boolean {
        val id = blankNode.blankNodeId

        id.toCharArray()
            .forEachIndexed { index, char ->
                if (
                    isValidBlankNodeChar(
                        charPosition = index,
                        char = char,
                        idLength = id.length
                    )
                ) return false
            }
        return true
    }


    fun <T : RdfTerm> encode(rdfTerm: T): T {
        return when (rdfTerm) {
            is BlankNode -> {
                return rdfTerm.copy(encodeBlankNode(rdfTerm.blankNodeId)) as T
            }

            else -> rdfTerm
        }
    }

    private fun encodeBlankNode(string: String): String {
        val hexValueForO = Integer.toHexString('O'.code).uppercase().padStart(4, '0')
        val hexValueForx = Integer.toHexString('x'.code).uppercase().padStart(4, '0')
        val strWithoutOx = string.replace("Ox([0-9A-Fa-f]{4})".toRegex()) {
            "Ox${hexValueForO}Ox$hexValueForx" + it.destructured.component1()
        }
        return buildString {
            strWithoutOx.toCharArray().forEachIndexed { i, char ->
                if (isValidBlankNodeChar(i, char, strWithoutOx.length)) {
                    this.append(char)
                } else {
                    val hexValue = Integer.toHexString(char.code).padStart(4, '0').uppercase()
                    this.append("Ox$hexValue")
                }
            }
        }
    }

    private fun isValidBlankNodeChar(charPosition: Int, char: Char, idLength: Int): Boolean {
        val pnCharsRegex = "$pnChars".toRegex()
        val pnCharsWithDotRegex = "($pnChars|\\.)".toRegex()
        val pnCharsURegex = "$pnCharsU".toRegex()

        val isValidFirstChar = (charPosition == 0) && (pnCharsURegex.matches(char.toString()) || char.isDigit())
        val isValidMiddleChar = (charPosition in 1..(idLength - 2) && pnCharsWithDotRegex.matches(char.toString()))
        val isValidLastChar = charPosition == (idLength - 1) && pnCharsRegex.matches(char.toString())

        return isValidMiddleChar || isValidFirstChar || isValidLastChar
    }
}
