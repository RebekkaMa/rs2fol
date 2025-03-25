package app.interfaces.services.presenter

import util.commandResult.RootError

interface ErrorToStringTransformerService {
    operator fun invoke(error: RootError): String
}