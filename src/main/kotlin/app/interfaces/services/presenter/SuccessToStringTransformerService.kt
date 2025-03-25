package app.interfaces.services.presenter

import util.commandResult.Success

interface SuccessToStringTransformerService {
    operator fun invoke(success: Success): String?
}