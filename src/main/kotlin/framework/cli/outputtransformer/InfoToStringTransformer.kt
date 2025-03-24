package framework.cli.outputtransformer

object InfoToStringTransformer {
    operator fun invoke(message: String): String {
        return "% ... ${TextStyler.info(message)}"
    }
}