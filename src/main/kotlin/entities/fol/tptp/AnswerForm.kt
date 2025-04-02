package entities.fol.tptp

import entities.fol.GeneralTerm

data class TPTPTupleAnswerFormAnswer(
    val answerTuples: List<AnswerTuple> = emptyList(),
    val disjunctiveAnswerTuples: List<List<AnswerTuple>> = emptyList()
)

data class AnswerTuple(val answers: List<GeneralTerm>) : List<GeneralTerm> by answers
