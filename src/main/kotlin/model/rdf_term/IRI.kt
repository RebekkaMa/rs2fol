package model.rdf_term

data class IRI(
    val scheme: String? = null,
    val authority: String? = null,
    val path: String,
    val query: String? = null,
    val fragment: String? = null,
) : RdfTerm() {

    val iri: String = componentRecomposition(
        scheme,
        authority,
        path,
        query,
        fragment
    )

    companion object {

        fun from(fullIRI: String): IRI {
            return "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?"
                .toRegex()
                .matchEntire(fullIRI)
                ?.let { matchResult ->
                    val (component1, component2, component3,
                        component4, component5, component6,
                        component7, component8, component9) = matchResult.destructured
                    IRI(
                        scheme = component2.takeUnless { component1.isEmpty() },
                        authority = component4.takeUnless { component3.isEmpty() },
                        path = component5,
                        query = component7.takeUnless { component6.isEmpty() },
                        fragment = component9.takeUnless { component8.isEmpty() }
                    )
                } ?: IRI(path = fullIRI)
        }

        fun componentRecomposition(
            scheme: String? = null,
            authority: String? = null,
            path: String,
            query: String? = null,
            fragment: String? = null,
        ) =
            buildString {
                if (scheme != null) append("$scheme:")
                if (authority != null) append("//$authority")
                append(path)
                if (query != null) append("?$query")
                if (fragment != null) append("#$fragment")
            }

        fun transformReference(
            R: IRI,
            B: IRI
        ): IRI {
            fun merge(): String =
                if (B.authority != null && B.path.isEmpty()) "/${R.path}" else (B.path.dropLastWhile { it != '/' } + R.path)

            fun removeDotSegments(path: String): String {

                val inputBuffer = StringBuilder(path)
                val outputBuffer = StringBuilder()

                while (inputBuffer.isNotBlank()) {
                    when {
                        inputBuffer.startsWith("../") -> inputBuffer.deleteRange(0, 3)
                        inputBuffer.startsWith("./") -> inputBuffer.deleteRange(0, 2)
                        inputBuffer.startsWith("/./") -> inputBuffer.setRange(0, 3, "/")
                        inputBuffer.contentEquals("/.") -> inputBuffer.setRange(0, 2, "/")
                        inputBuffer.startsWith("/../") -> {
                            inputBuffer.setRange(0, 4, "/")
                            outputBuffer.delete(outputBuffer.indexOfLast { it == '/' }.takeUnless { it == -1 } ?: 0,
                                outputBuffer.lastIndex + 1)
                        }

                        inputBuffer.contentEquals("/..") -> {
                            inputBuffer.setRange(0, 3, "/")
                            outputBuffer.delete(outputBuffer.indexOfLast { it == '/' }.takeUnless { it == -1 } ?: 0,
                                outputBuffer.lastIndex + 1)
                        }

                        inputBuffer.contentEquals("..") || inputBuffer.contentEquals(".") -> inputBuffer.clear()
                        else -> {
                            val switch =
                                "^(/?[^/]*)(/|$)".toRegex().find(inputBuffer)?.groups?.get(1)?.range
                            if (switch != null) {
                                outputBuffer.append(inputBuffer, switch.first, switch.last + 1)
                                inputBuffer.delete(switch.first, switch.last + 1)
                            }
                        }
                    }
                }
                return outputBuffer.toString()
            }

            val targetURIScheme: String?
            val targetURIauthority: String?
            val targetURIpath: String?
            val targetURIquery: String?

            if (R.scheme != null) {
                targetURIScheme = R.scheme
                targetURIauthority = R.authority
                targetURIpath = removeDotSegments(R.path)
                targetURIquery = R.query
            } else {
                if (R.authority != null) {
                    targetURIauthority = R.authority
                    targetURIpath = removeDotSegments(R.path)
                    targetURIquery = R.query
                } else {
                    if (R.path.isEmpty()) {
                        targetURIpath = B.path
                        targetURIquery = R.query ?: B.query
                    } else {
                        targetURIpath = removeDotSegments(R.path.takeIf { R.path.startsWith("/") } ?: merge())
                        targetURIquery = R.query
                    }
                    targetURIauthority = B.authority
                }
                targetURIScheme = B.scheme
            }

            val targetURIfragment: String? = R.fragment

            return IRI(
                targetURIScheme,
                targetURIauthority,
                targetURIpath,
                targetURIquery,
                targetURIfragment
            )
        }
    }

    fun getIRIWithoutFragment(): String {
        val index = iri.lastIndexOf('#')
        return if (index == -1) iri else iri.substring(0, index + 1)
    }

    fun isRelativeReference() = scheme.isNullOrBlank()

    override fun toString(): String = iri

}