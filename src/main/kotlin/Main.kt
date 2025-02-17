import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import interface_adapters.cli.Rs2fol
import interface_adapters.cli.subcommands.*

suspend fun main(args: Array<String>) =
    Rs2fol().subcommands(
        Rewrite(),
        Transform(),
        QaAnswerToRs(),
        Check(),
        TransformQa(),
    )
        .main(args)