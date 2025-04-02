package adapter.coder

import app.interfaces.services.coder.FOLCoderService
import entities.fol.*

class FOLCoderServiceImpl : FOLCoderService {

    override fun encode(folModel: FOLExpression): FOLExpression {

        fun transformToValidTerm(folTerm: GeneralTerm): GeneralTerm {
            return when (folTerm) {
                is FOLConstant -> FOLConstant(encodeLiteral(folTerm.name))
                is FOLFunction -> folTerm.copy(arguments = folTerm.arguments.map { transformToValidTerm(it) })
                is FOLVariable -> FOLVariable(encodeVariable(folTerm.name))
            }
        }

        return when (folModel) {
            is FOLConstant -> transformToValidTerm(folModel)
            is FOLFunction -> transformToValidTerm(folModel)
            is FOLVariable -> transformToValidTerm(folModel)
            is FOLAnd -> FOLAnd(folModel.expressions.map { encode(it) })
            is FOLEquivalent -> FOLEquivalent(encode(folModel.left), encode(folModel.right))
            is FOLImplies -> FOLImplies(encode(folModel.left), encode(folModel.right))
            is FOLNot -> FOLNot(encode(folModel.expression))
            is FOLOr -> FOLOr(folModel.expressions.map { encode(it) })
            is FOLPredicate -> FOLPredicate(folModel.name, folModel.arguments.map { encode(it) })
            is FOLExists -> FOLExists(
                folModel.variables.map { FOLVariable(encodeVariable(it.name)) },
                encode(folModel.expression)
            )

            is FOLForAll -> FOLForAll(
                folModel.variables.map { FOLVariable(encodeVariable(it.name)) },
                encode(folModel.expression)
            )
        }
    }

    override fun decode(folModel: FOLExpression): FOLExpression {

        fun decode(folTerm: GeneralTerm): GeneralTerm {
            return when (folTerm) {
                is FOLConstant -> FOLConstant(decodeLiteral(folTerm.name))
                is FOLFunction -> folTerm.copy(arguments = folTerm.arguments.map { decode(it) })
                is FOLVariable -> FOLVariable(decodeVariable(folTerm.name))
            }
        }

        return when (folModel) {
            is FOLConstant -> decode(folModel)
            is FOLFunction -> decode(folModel)
            is FOLVariable -> decode(folModel)
            is FOLAnd -> FOLAnd(folModel.expressions.map { decode(it) })
            is FOLEquivalent -> FOLEquivalent(decode(folModel.left), decode(folModel.right))
            is FOLImplies -> FOLImplies(decode(folModel.left), decode(folModel.right))
            is FOLNot -> FOLNot(decode(folModel.expression))
            is FOLOr -> FOLOr(folModel.expressions.map { decode(it) })
            is FOLPredicate -> FOLPredicate(folModel.name, folModel.arguments.map { decode(it) })
            is FOLExists -> FOLExists(
                folModel.variables.map { FOLVariable(decodeVariable(it.name)) },
                decode(folModel.expression)
            )

            is FOLForAll -> FOLForAll(
                folModel.variables.map { FOLVariable(decodeVariable(it.name)) },
                decode(folModel.expression)
            )
        }
    }

    private fun decodeLiteral(string: String) = string.replace("\\\\\\\\u[0-9A-Fa-f]{4}".toRegex()) {
        Integer.parseInt(it.value.drop(3), 16).toChar().toString()
    }

    private fun decodeVariable(string: String) = string.replace("Ox[0-9A-Fa-f]{4}".toRegex()) {
        Integer.parseInt(it.value.drop(2), 16).toChar().toString()
    }

    private fun encodeLiteral(string: String) = buildString {
        string.toCharArray().forEach { c ->
            if (isPrintableAscii(c) && c != '\\' && c != '\'') {
                this.append(c)
            } else {
                val hexValue = Integer.toHexString(c.code)
                this.append("\\\\u${hexValue.padStart(4, '0').uppercase()}")
            }
        }
    }

    private fun encodeVariable(string: String) = buildString {
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

    private fun isPrintableAscii(char: Char) = char.code in 32..127

}