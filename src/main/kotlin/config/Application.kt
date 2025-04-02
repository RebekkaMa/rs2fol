package config

import adapter.coder.FOLCoderServiceImpl
import adapter.coder.N3SRDFTermCoderServiceImpl
import adapter.file.FileServiceImpl
import adapter.jena.XSDLiteralServiceImpl
import adapter.parser.RDFSurfaceParserServiceImpl
import adapter.parser.SZSParserServiceImpl
import adapter.parser.TPTPTupleAnswerFormToModelServiceImpl
import adapter.presenter.*
import adapter.theoremProver.ConfigLoaderServiceImpl
import adapter.theoremProver.TheoremProverRunnerServiceImpl
import app.interfaces.services.*
import app.interfaces.services.coder.FOLCoderService
import app.interfaces.services.coder.N3SRDFTermCoderService
import app.interfaces.services.jena.XSDLiteralService
import app.interfaces.services.presenter.ErrorToStringTransformerService
import app.interfaces.services.presenter.InfoToStringTransformerService
import app.interfaces.services.presenter.SuccessToStringTransformerService
import app.interfaces.services.presenter.TextStylerService
import app.use_cases.commands.*
import app.use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
import app.use_cases.commands.subUseCase.QuestionAnsweringOutputToRDFSurfacesCascUseCase
import app.use_cases.commands.subUseCase.TPTPTupleAnswerModelToN3SUseCase
import app.use_cases.modelToString.FOLModelToFOFFormulaStringUseCase
import app.use_cases.modelToString.RdfSurfaceModelToN3UseCase
import app.use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import app.use_cases.modelTransformer.CanoncicalizeRDFSurfaceLiteralsUseCase
import app.use_cases.modelTransformer.FOLGeneralTermToRDFTermUseCase
import app.use_cases.modelTransformer.RDFSurfaceModelToFOLModelUseCase
import app.use_cases.modelTransformer.RDFSurfaceModelToTPTPAnnotatedFormulaUseCase


object Application {
    fun createCheckUseCase(): CheckUseCase {
        return CheckUseCase(
            theoremProverRunnerService = createTheoremProverRunnerService(),
            szsParserService = createSzsParserService(),
            transformUseCase = createTransformUseCase(),
            getTheoremProverCommandUseCase = createGetTheoremProverCommandUseCase(),
        )
    }

    fun createCascQaAnswerToRsUseCase(): CascQaAnswerToRSUseCase {
        return CascQaAnswerToRSUseCase(
            rdfSurfaceParserService = createRDFSurfaceParseService(),
            fileService = createFileService(),
            questionAnsweringOutputToRdfSurfacesCascUseCase = createQuestionAnsweringOutputToRdfSurfacesCascUseCase(),
            successToStringTransformerService = createFileSuccessToStringTransformerService()
        )
    }

    fun createRawQaAnswerToRsUseCase(): RawQaAnswerToRSUseCase {
        return RawQaAnswerToRSUseCase(
            rdfSurfaceParserService = createRDFSurfaceParseService(),
            tPTPTupleAnswerModelToN3SUseCase = createTptpTupleAnswerModelToRdfSurfaceUseCase(),
            fileService = createFileService(),
            tptpTupleAnswerFormParserService = createTptpTupleAnswerFormParserService(),
        )
    }

    fun createRewriteUseCase(): RewriteUseCase {
        return RewriteUseCase(
            fileService = createFileService(),
            rdfSurfaceParserService = createRDFSurfaceParseService(),
            rdfSurfaceModelToN3UseCase = createRdfSurfaceModelToN3UseCase(),
            canoncicalizeRDFSurfaceLiteralsUseCase = createCanonicalizeRDFSurfaceLiteralsUseCase()
        )
    }

    fun createTransformQaUseCase(): TransformQaUseCase {
        return TransformQaUseCase(
            rdfSurfaceParserService = createRDFSurfaceParseService(),
            theoremProverRunnerService = createTheoremProverRunnerService(),
            fileService = createFileService(),
            tPTPAnnotatedFormulaModelToStringUseCase = createTPTPAnnotatedFormulaModelToStringUseCase(),
            rdfSurfaceModelToTPTPAnnotatedFormulaUseCase = createRdfSurfaceModelToTPTPModelUseCase(),
            getTheoremProverCommandUseCase = createGetTheoremProverCommandUseCase(),
            questionAnsweringOutputToRdfSurfacesCascUseCase = createQuestionAnsweringOutputToRdfSurfacesCascUseCase(),
            canoncicalizeRDFSurfaceLiteralsUseCase = createCanonicalizeRDFSurfaceLiteralsUseCase()

        )
    }

    private fun createQuestionAnsweringOutputToRdfSurfacesCascUseCase(): QuestionAnsweringOutputToRDFSurfacesCascUseCase {
        return QuestionAnsweringOutputToRDFSurfacesCascUseCase(
            tptpTupleAnswerModelToN3SUseCase = createTptpTupleAnswerModelToRdfSurfaceUseCase(),
            szsParserService = createSzsParserService()
        )
    }

    private fun createTptpTupleAnswerModelToRdfSurfaceUseCase(): TPTPTupleAnswerModelToN3SUseCase {
        return TPTPTupleAnswerModelToN3SUseCase(
            rdfSurfaceModelToN3UseCase = createRdfSurfaceModelToN3UseCase(),
            fOLGeneralTermToRDFTermUseCase = createFolGeneralTermToRdfTermUseCase(),
            canoncicalizeRDFSurfaceLiteralsUseCase = createCanonicalizeRDFSurfaceLiteralsUseCase()
        )
    }

    private fun createFolGeneralTermToRdfTermUseCase(): FOLGeneralTermToRDFTermUseCase {
        return FOLGeneralTermToRDFTermUseCase()
    }

    fun createTransformUseCase(): TransformUseCase {
        return TransformUseCase(
            fileService = createFileService(),
            rdfSurfaceParserService = createRDFSurfaceParseService(),
            tPTPAnnotatedFormulaModelToStringUseCase = createTPTPAnnotatedFormulaModelToStringUseCase(),
            rdfSurfaceModelToTPTPAnnotatedFormulaUseCase = createRdfSurfaceModelToTPTPModelUseCase(),
            canoncicalizeRDFSurfaceLiteralsUseCase = createCanonicalizeRDFSurfaceLiteralsUseCase()
        )
    }

    private fun createRdfSurfaceModelToN3UseCase(): RdfSurfaceModelToN3UseCase {
        return RdfSurfaceModelToN3UseCase(
            n3SRDFTermCoderService = createN3SRDFTermCoderService()
        )
    }

    private fun createGetTheoremProverCommandUseCase(): GetTheoremProverCommandUseCase {
        return GetTheoremProverCommandUseCase(
            createConfigLoaderService(),
        )
    }


    private fun createTPTPAnnotatedFormulaModelToStringUseCase(): TPTPAnnotatedFormulaModelToStringUseCase {
        return TPTPAnnotatedFormulaModelToStringUseCase(
            fOLModelToFOFFormulaStringUseCase = createFOLModelToStringUseCase(),
            fOLCoderService = createFOLCoderService(),
        )
    }

    private fun createFOLModelToStringUseCase(): FOLModelToFOFFormulaStringUseCase {
        return FOLModelToFOFFormulaStringUseCase()
    }

    private fun createRdfSurfaceModelToTPTPModelUseCase(): RDFSurfaceModelToTPTPAnnotatedFormulaUseCase {
        return RDFSurfaceModelToTPTPAnnotatedFormulaUseCase(createRdfSurfaceModelToFOLModelUseCase())
    }

    private fun createCanonicalizeRDFSurfaceLiteralsUseCase(): CanoncicalizeRDFSurfaceLiteralsUseCase {
        return CanoncicalizeRDFSurfaceLiteralsUseCase(createLiteralService())
    }

    private fun createRdfSurfaceModelToFOLModelUseCase(): RDFSurfaceModelToFOLModelUseCase {
        return RDFSurfaceModelToFOLModelUseCase()
    }

    private fun createFileService(): FileService {
        return FileServiceImpl()
    }

    private fun createSzsParserService(): SZSParserService {
        return SZSParserServiceImpl(
            tptpTupleAnswerFormToModelService = createTptpTupleAnswerFormParserService()
        )
    }

    private fun createRDFSurfaceParseService(): RDFSurfaceParserService {
        return RDFSurfaceParserServiceImpl()
    }

    private fun createConfigLoaderService(): ConfigLoaderService {
        return ConfigLoaderServiceImpl()
    }

    private fun createTheoremProverRunnerService(): TheoremProverRunnerService {
        return TheoremProverRunnerServiceImpl()
    }

    private fun createTptpTupleAnswerFormParserService(): TPTPTupleAnswerFormParserService {
        return TPTPTupleAnswerFormToModelServiceImpl()
    }

    private fun createFOLCoderService(): FOLCoderService {
        return FOLCoderServiceImpl()
    }

    private fun createN3SRDFTermCoderService(): N3SRDFTermCoderService {
        return N3SRDFTermCoderServiceImpl()
    }

    fun createCliSuccessToStringTransformerService(): SuccessToStringTransformerService {
        return SuccessToStringTransformerServiceImpl(createCliTextStylerService())
    }

    private fun createFileSuccessToStringTransformerService(): SuccessToStringTransformerService {
        return SuccessToStringTransformerServiceImpl(createFileTextStylerService())
    }

    fun createErrorToStringTransformerService(): ErrorToStringTransformerService {
        return ErrorToStringTransformerServiceImpl(createCliTextStylerService())
    }

    fun createInfoToStringTransformerService(): InfoToStringTransformerService {
        return InfoToStringTransformerServiceImpl(createCliTextStylerService())
    }

    private fun createFileTextStylerService(): TextStylerService {
        return FileTextStylerServiceImpl()
    }

    private fun createCliTextStylerService(): TextStylerService {
        return AjaltMordantTextStylerServiceImpl()
    }

    private fun createLiteralService(): XSDLiteralService {
        return XSDLiteralServiceImpl()
    }
}