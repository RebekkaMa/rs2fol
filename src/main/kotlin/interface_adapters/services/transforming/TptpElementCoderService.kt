package interface_adapters.services.transforming

object TptpElementCoderService {

    fun decodeToValidTPTPLiteral(string: String)  = string.replace("\\\\\\\\u[0-9A-Fa-f]{4}".toRegex()) {
            Integer.parseInt(it.value.drop(3), 16).toChar().toString()
        }

    fun decodeToValidTPTPVariable(string: String) = string.replace("Ox[0-9A-Fa-f]{4}".toRegex()) {
        Integer.parseInt(it.value.drop(2), 16).toChar().toString()
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

    fun encodeToValidTPTPVariable(string: String) = buildString {
        val hexValueForO = Integer.toHexString('O'.code).uppercase().padStart(4, '0')
        val hexValueForx = Integer.toHexString('x'.code).uppercase().padStart(4, '0')
        val strWithoutOx = string.replace("Ox([0-9A-Fa-f]{4})".toRegex()) {
            "Ox${hexValueForO}Ox$hexValueForx" + it.destructured.component1()
        }
        strWithoutOx.toCharArray().forEachIndexed { index, c ->
            if ((isPrintableAscii(c)) && (index != 0 || c.isUpperCase())) {
                append(c)
            } else {
                val hexValue = Integer.toHexString(c.code).uppercase().padStart(4, '0')
                append("Ox$hexValue")
            }
        }
    }

    fun isPrintableAscii(char: Char) = char.code in 32..127

}