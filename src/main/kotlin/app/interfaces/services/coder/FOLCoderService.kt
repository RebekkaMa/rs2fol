package app.interfaces.services.coder

import entities.fol.FOLExpression

interface FOLCoderService {
    fun encode(folModel: FOLExpression): FOLExpression
    fun decode(folModel: FOLExpression): FOLExpression
}
