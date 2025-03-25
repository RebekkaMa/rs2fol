package app.interfaces.services.presenter

interface InfoToStringTransformerService {
    operator fun invoke(message: String): String
}