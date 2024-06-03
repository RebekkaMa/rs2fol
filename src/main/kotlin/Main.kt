import com.github.ajalt.clikt.core.*
import interface_adapters.cli.Rs2fol
import interface_adapters.cli.subcommands.*

fun main(args: Array<String>) =
    Rs2fol().subcommands(
        Rewrite(),
        Transform(),
        QaAnswerToRs(),
        Check(),
        TransformQa(),
    )
        .main(args)