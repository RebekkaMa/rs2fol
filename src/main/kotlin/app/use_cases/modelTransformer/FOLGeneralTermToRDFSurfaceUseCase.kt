package app.use_cases.modelTransformer

import app.use_cases.results.modelTransformerResults.FOLGeneralTermToRDFSurfaceResult
import entities.fol.FOLConstant
import entities.fol.FOLFunction
import entities.fol.FOLVariable
import entities.fol.GeneralTerm
import entities.rdfsurfaces.rdf_term.*
import entities.rdfsurfaces.rdf_term.Collection
import util.commandResult.*

class FOLGeneralTermToRDFTermUseCase {

    operator fun invoke(generalTerm: GeneralTerm): Result<RDFTerm, RootError> {
        return when (generalTerm) {
            is FOLVariable -> success(BlankNode(generalTerm.name))
            is FOLFunction -> {
                val functionName = generalTerm.name
                when {
                    functionName.startsWith("sk", ignoreCase = true) -> {
                        success(
                            BlankNode(
                                generalTerm.name + "-" + generalTerm.arguments.hashCode()
                            )
                        )
                    }

                    functionName == "list" -> {
                        success(
                            generalTerm.arguments.map { invoke(it).getOrElse { err -> return error(err) } }
                                .let { Collection(it) }
                        )
                    }

                    else -> {
                        error(FOLGeneralTermToRDFSurfaceResult.Error.InvalidFunctionOrPredicate(functionName))
                    }
                }
            }

            is FOLConstant -> {
                val constantName = generalTerm.name
                when {
                    constantName.startsWith("sk",ignoreCase = true) -> success(BlankNode(constantName))
                    constantName.startsWith("list") -> success(Collection(emptyList()))
                    else -> {
                        (getLiteralFromStringOrNull(constantName)
                            ?: getLangLiteralFromStringOrNull(constantName)
                            ?: IRI.from(constantName)
                                .takeUnless { it.isRelativeReference() || it.iri.contains("\\s".toRegex()) }
                                )?.let { success(it) } ?: error(
                            FOLGeneralTermToRDFSurfaceResult.Error.InvalidElement(element = constantName)
                        )
                    }
                }
            }
        }
    }

    private fun getLiteralFromStringOrNull(literal: String) =
        ("\"(.*)\"\\^\\^(.+)".toRegex()).matchEntire(literal)?.let {
            val (lexicalValue, datatypeIri) = it.destructured
            DefaultLiteral(lexicalValue, IRI.from(datatypeIri))
        }


    private fun getLangLiteralFromStringOrNull(literal: String) =
        ("\"(.*)\"@(.+)".toRegex()).matchEntire(literal)?.let {
            val (literalValue, languageTag) = it.destructured
            LanguageTaggedString(
                lexicalValue = literalValue,
                langTag = languageTag
            )
        }
}
