package use_cases.modelTransformer

import entities.fol.FOLConstant
import entities.fol.FOLFunction
import entities.fol.FOLVariable
import entities.fol.GeneralTerm
import entities.rdfsurfaces.rdf_term.*
import entities.rdfsurfaces.rdf_term.Collection
import interface_adapters.services.parsing.InvalidElementException
import util.SurfaceNotSupportedException

object FOLGeneralTermToRDFTermUseCase {

    operator fun invoke(generalTerm: GeneralTerm): RdfTerm {
        return when (generalTerm) {
            is FOLVariable -> BlankNode(generalTerm.name)
            is FOLFunction -> {
                val functionName = generalTerm.name
                when {
                    functionName.startsWith("sk") -> {
                        BlankNode(
                            generalTerm.name + "-" + generalTerm.arguments.hashCode()
                        )
                    }

                    functionName == "list" -> {
                        Collection(generalTerm.arguments.map { invoke(it) })
                    }

                    else -> {
                        throw SurfaceNotSupportedException("Function $functionName is not supported")
                    }
                }
            }

            is FOLConstant -> {
                val constantName = generalTerm.name
                when {
                    constantName.startsWith("sk") -> BlankNode(constantName)
                    constantName.startsWith("list") -> Collection(emptyList())
                    else -> {
                        getLiteralFromStringOrNull(constantName)
                            ?: getLangLiteralFromStringOrNull(constantName)
                            ?: IRI.from(constantName)
                                .takeUnless { it.isRelativeReference() || it.iri.contains("\\s".toRegex()) }
                            ?: throw InvalidElementException(element = constantName)
                    }
                }
            }
        }
    }

    private fun getLiteralFromStringOrNull(literal: String) =
        ("\"(.*)\"\\^\\^(.+)".toRegex()).matchEntire(literal)?.let {
            val (literalValue, datatypeIri) = it.destructured
            DefaultLiteral.fromNonNumericLiteral(literalValue, IRI.from(datatypeIri))
        }


    private fun getLangLiteralFromStringOrNull(literal: String) =
        ("\"(.*)\"@(.+)".toRegex()).matchEntire(literal)?.let {
            val (literalValue, languageTag) = it.destructured
            LanguageTaggedString(literalValue, languageTag)
        }
}