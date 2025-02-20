package services.parser

import entities.fol.FOLConstant
import entities.fol.FOLFunction
import entities.fol.tptp.AnswerTuple
import entities.fol.tptp.TPTPTupleAnswerFormAnswer
import interface_adapters.services.parser.TptpTupleAnswerFormToModelService
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import util.commandResult.getSuccessOrNull

class TPTPTupleAnswerFormParserToFOLTest : ShouldSpec(
    {
        should("parse basic example without exception") {
            val str =
                "[(['http://example.org/ns#beetle','http://example.org/ns#nice']|['http://example.org/ns#beetle','http://example.org/ns#green']),['http://example.org/ns#beetle','http://example.org/ns#beautiful']|_]"
            TptpTupleAnswerFormToModelService.parseToEnd(str).getSuccessOrNull()
                .shouldNotBeNull() shouldBeEqual TPTPTupleAnswerFormAnswer(
                answerTuples = listOf(
                    AnswerTuple(
                        listOf(
                            FOLConstant("http://example.org/ns#beetle"),
                            FOLConstant("http://example.org/ns#beautiful")
                        )
                    )
                ),
                disjunctiveAnswerTuples = listOf(
                    listOf(
                        AnswerTuple(
                            listOf(
                                FOLConstant("http://example.org/ns#beetle"),
                                FOLConstant("http://example.org/ns#nice")
                            )
                        ),
                        AnswerTuple(
                            listOf(
                                FOLConstant("http://example.org/ns#beetle"),
                                FOLConstant("http://example.org/ns#green")
                            )
                        ),

                        )
                )
            )
        }

        should("parse sec basic example without exception") {
            val str =
                "[(['http://example.org/ns#i', 'http://example.org/ns#D']|['http://example.org/ns#i', 'http://example.org/ns#B'])|_]"
            TptpTupleAnswerFormToModelService.parseToEnd(str).getSuccessOrNull()
                .shouldNotBeNull() shouldBeEqual TPTPTupleAnswerFormAnswer(
                answerTuples = listOf(),
                disjunctiveAnswerTuples = listOf(
                    listOf(
                        AnswerTuple(
                            listOf(
                                FOLConstant("http://example.org/ns#i"),
                                FOLConstant("http://example.org/ns#D")
                            )
                        ),
                        AnswerTuple(
                            listOf(
                                FOLConstant("http://example.org/ns#i"),
                                FOLConstant("http://example.org/ns#B")
                            )
                        )
                    )
                )
            )
        }


        should("parse example with lists without exception") {
            val str =
                "[[list('http://example.org/ns#s',list('http://example.org/ns#s',list('http://example.org/ns#s','\"0\"^^http://www.w3.org/2001/XMLSchema#integer'))),list]|_]"
            TptpTupleAnswerFormToModelService.parseToEnd(str).getSuccessOrNull()
                .shouldNotBeNull() shouldBeEqual TPTPTupleAnswerFormAnswer(
                listOf(
                    AnswerTuple(
                        listOf(
                            FOLFunction(
                                "list",
                                listOf(
                                    FOLConstant("http://example.org/ns#s"),
                                    FOLFunction(
                                        "list",
                                        listOf(
                                            FOLConstant("http://example.org/ns#s"),
                                            FOLFunction(
                                                "list",
                                                listOf(
                                                    FOLConstant("http://example.org/ns#s"),
                                                    FOLConstant("\"0\"^^http://www.w3.org/2001/XMLSchema#integer")
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            FOLConstant("list")
                        )
                    )
                ),
                listOf()
            )
        }

        should("parse example with rdf literals without exception") {
            val str =
                "[['\"0\"^^http://www.w3.org/2001/XMLSchema#string','\"0\"@en'],['http://example.org/ns#s', sK5]|_]"
            TptpTupleAnswerFormToModelService.parseToEnd(str).getSuccessOrNull()
                .shouldNotBeNull() shouldBeEqual TPTPTupleAnswerFormAnswer(
                listOf(
                    AnswerTuple(
                        listOf(
                            FOLConstant("\"0\"^^http://www.w3.org/2001/XMLSchema#string"),
                            FOLConstant("\"0\"@en"),
                        )
                    ),
                    AnswerTuple(
                        listOf(
                            FOLConstant("http://example.org/ns#s"),
                            FOLConstant("sK5")
                        )
                    )
                ),
                listOf()
            )
        }


        should("parse example with skolem function without exception") {
            val str = "[[sK1('http://example.org/ns#b','http://example.org/ns#c')]|_]"
            TptpTupleAnswerFormToModelService.parseToEnd(str).getSuccessOrNull()
                .shouldNotBeNull() shouldBeEqual TPTPTupleAnswerFormAnswer(
                listOf(
                    AnswerTuple(
                        listOf(
                            FOLFunction(
                                "sK1",
                                listOf(
                                    FOLConstant("http://example.org/ns#b"),
                                    FOLConstant("http://example.org/ns#c")
                                )
                            )
                        )
                    )
                ),
                listOf()
            )
        }

        should("parse empty result") {
            val str = "[|_]"
            TptpTupleAnswerFormToModelService.parseToEnd(str).getSuccessOrNull()
                .shouldNotBeNull() shouldBeEqual TPTPTupleAnswerFormAnswer(listOf(), listOf())
        }

        should("parse another basic example without exception") {
            val str =
                "[['http://example.org/ns#beetle','\"RDF/XML Syntax Specification (Revised)\"^^http://www.w3.org/2001/XMLSchema#string'],[list('http://example.org/ns#s'),'\"That Seventies Show\"@en'],['http://example.org/ns#beetle','\"http://www.w3.org/2001/XMLSchema#string\"^^http://www.w3.org/2001/XMLSchema#string']|_]"
            TptpTupleAnswerFormToModelService.parseToEnd(str).getSuccessOrNull()
                .shouldNotBeNull() shouldBeEqual TPTPTupleAnswerFormAnswer(

                listOf(
                    AnswerTuple(
                        listOf(
                            FOLConstant("http://example.org/ns#beetle"),
                            FOLConstant("\"RDF/XML Syntax Specification (Revised)\"^^http://www.w3.org/2001/XMLSchema#string"),
                        )
                    ),
                    AnswerTuple(
                        listOf(
                            FOLFunction(
                                "list",
                                listOf(
                                    FOLConstant("http://example.org/ns#s")
                                )
                            ),
                            FOLConstant("\"That Seventies Show\"@en")
                        )
                    ),
                    AnswerTuple(
                        listOf(
                            FOLConstant("http://example.org/ns#beetle"),
                            FOLConstant("\"http://www.w3.org/2001/XMLSchema#string\"^^http://www.w3.org/2001/XMLSchema#string")
                        )
                    ),
                ),
                listOf()
            )
        }
    }
)
