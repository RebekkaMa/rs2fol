package app.use_cases.modelTransformer

import entities.fol.FOLConstant
import entities.fol.FOLFunction
import entities.fol.FOLVariable
import entities.fol.GeneralTerm
import entities.rdfsurfaces.rdf_term.*
import entities.rdfsurfaces.rdf_term.Collection
import util.commandResult.*

class FOLGeneralTermToRDFTermUseCase {

    operator fun invoke(generalTerm: GeneralTerm): IntermediateStatus<RdfTerm, RootError> {
        return when (generalTerm) {
            is FOLVariable -> intermediateSuccess(BlankNode(generalTerm.name))
            is FOLFunction -> {
                val functionName = generalTerm.name
                when {
                    functionName.startsWith("sk") -> {
                        intermediateSuccess(
                            BlankNode(
                                generalTerm.name + "-" + generalTerm.arguments.hashCode()
                            )
                        )
                    }

                    functionName == "list" -> {
                        intermediateSuccess(
                            generalTerm.arguments.map { invoke(it).getOrElse { err -> return intermediateError(err) } }
                                .let { Collection(it) }
                        )
                    }

                    else -> {
                        intermediateError(FOLGeneralTermToRDFSurfaceUseCaseError.InvalidFunctionOrPredicate(functionName))
                    }
                }
            }

            is FOLConstant -> {
                val constantName = generalTerm.name
                when {
                    constantName.startsWith("sk") -> intermediateSuccess(BlankNode(constantName))
                    constantName.startsWith("list") -> intermediateSuccess(Collection(emptyList()))
                    else -> {
                        (getLiteralFromStringOrNull(constantName)
                            ?: getLangLiteralFromStringOrNull(constantName)
                            ?: IRI.from(constantName)
                                .takeUnless { it.isRelativeReference() || it.iri.contains("\\s".toRegex()) }
                                )?.let { intermediateSuccess(it) } ?: intermediateError(
                            FOLGeneralTermToRDFSurfaceUseCaseError.InvalidElement(element = constantName)
                        )
                    }
                }
            }
        }
    }

    private fun getLiteralFromStringOrNull(literal: String) =
        ("\"(.*)\"\\^\\^(.+)".toRegex()).matchEntire(literal)?.let {
            val (literalValue, datatypeIri) = it.destructured
            DefaultLiteral(literalValue, IRI.from(datatypeIri))
        }


    private fun getLangLiteralFromStringOrNull(literal: String) =
        ("\"(.*)\"@(.+)".toRegex()).matchEntire(literal)?.let {
            val (literalValue, languageTag) = it.destructured
            LanguageTaggedString(literalValue, languageTag)
        }
}

sealed interface FOLGeneralTermToRDFSurfaceUseCaseError : Error {
    data class InvalidFunctionOrPredicate(val element: String) : FOLGeneralTermToRDFSurfaceUseCaseError
    data class InvalidElement(val element: String) : FOLGeneralTermToRDFSurfaceUseCaseError
}
