import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import framework.cli.Rs2fol
import framework.cli.subcommands.*

suspend fun main(args: Array<String>) =
    Rs2fol().subcommands(
        Rewrite(),
        Transform(),
        QaAnswerToRs(),
        Check(),
        TransformQa(),
    )
        .main(args)