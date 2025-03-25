package adapter.presenter

import app.interfaces.services.presenter.InfoToStringTransformerService
import app.interfaces.services.presenter.TextStylerService

class InfoToStringTransformerServiceImpl(private val textStylerService: TextStylerService) : InfoToStringTransformerService {
    override operator fun invoke(message: String): String {
        return "% ... ${textStylerService.info(message)}"
    }
}