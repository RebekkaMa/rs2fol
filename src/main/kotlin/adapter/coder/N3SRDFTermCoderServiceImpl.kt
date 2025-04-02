package adapter.coder

import adapter.parser.util.pnChars
import adapter.parser.util.pnCharsU
import app.interfaces.services.coder.N3SRDFTermCoderService
import entities.rdfsurfaces.rdf_term.BlankNode
import entities.rdfsurfaces.rdf_term.RDFTerm

class N3SRDFTermCoderServiceImpl : N3SRDFTermCoderService {

    override fun <T : RDFTerm> encode(rdfTerm: T): T {
        return when (rdfTerm) {
            is BlankNode -> {
                return BlankNode(encodeBlankNodeId(rdfTerm.blankNodeId)) as T
            }

            else -> rdfTerm
        }
    }

    private fun encodeBlankNodeId(string: String): String {
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
