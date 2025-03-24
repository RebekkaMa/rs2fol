package config

import adapter.coder.FOLCoderService
import adapter.coder.N3SRDFTermCoderService
import adapter.file.FileServiceImpl
import adapter.parser.RDFSurfaceParseServiceImpl
import adapter.parser.SZSParserServiceImpl
import adapter.parser.TptpTupleAnswerFormToModelServiceImpl
import adapter.theoremProver.ConfigLoaderServiceImpl
import adapter.theoremProver.TheoremProverRunnerServiceImpl
import app.interfaces.services.*
import app.use_cases.commands.*
import app.use_cases.commands.subUseCase.GetTheoremProverCommandUseCase
import app.use_cases.commands.subUseCase.QuestionAnsweringOutputToRdfSurfacesCascUseCase
import app.use_cases.commands.subUseCase.TPTPTupleAnswerModelToRdfSurfaceUseCase
import app.use_cases.modelToString.FOLModelToStringUseCase
import app.use_cases.modelToString.RdfSurfaceModelToN3UseCase
import app.use_cases.modelToString.TPTPAnnotatedFormulaModelToStringUseCase
import app.use_cases.modelTransformer.FOLGeneralTermToRDFTermUseCase
import app.use_cases.modelTransformer.RdfSurfaceModelToTPTPModelUseCase


object Application {
    fun createCheckUseCase(): CheckUseCase {
        return CheckUseCase(
            theoremProverRunnerService = createTheoremProverRunnerService(),
            szsParserService = createSzsParserService(),
            fileService = createFileService(),
            transformUseCase = createTransformUseCase(),
            getTheoremProverCommandUseCase = createGetTheoremProverCommandUseCase(),
        )
    }

    fun createCascQaAnswerToRsUseCase(): CascQaAnswerToRsUseCase {
        return CascQaAnswerToRsUseCase(
            rdfSurfaceParseService = createRDFSurfaceParseService(),
            fileService = createFileService(),
            questionAnsweringOutputToRdfSurfacesCascUseCase = createQuestionAnsweringOutputToRdfSurfacesCascUseCase()
        )
    }

    fun createRawQaAnswerToRsUseCase(): CascQaAnswerToRsUseCase {
        return CascQaAnswerToRsUseCase(
            rdfSurfaceParseService = createRDFSurfaceParseService(),
            fileService = createFileService(),
            questionAnsweringOutputToRdfSurfacesCascUseCase = createQuestionAnsweringOutputToRdfSurfacesCascUseCase()
        )
    }

    fun createRewriteUseCase(): RewriteUseCase {
        return RewriteUseCase(
            fileService = createFileService(),
            rdfSurfaceParseService = createRDFSurfaceParseService(),
            rdfSurfaceModelToN3UseCase = createRdfSurfaceModelToN3UseCase()
        )
    }

    fun createTransformQaUseCase(): TransformQaUseCase {
        return TransformQaUseCase(
            rdfSurfaceParseService = createRDFSurfaceParseService(),
            theoremProverRunnerService = createTheoremProverRunnerService(),
            fileService = createFileService(),
            tPTPAnnotatedFormulaModelToStringUseCase = createTPTPAnnotatedFormulaModelToStringUseCase(),
            rdfSurfaceModelToTPTPModelUseCase = createRdfSurfaceModelToTPTPModelUseCase(),
            getTheoremProverCommandUseCase = createGetTheoremProverCommandUseCase(),
            questionAnsweringOutputToRdfSurfacesCascUseCase = createQuestionAnsweringOutputToRdfSurfacesCascUseCase()
        )
    }

    fun createQuestionAnsweringOutputToRdfSurfacesCascUseCase(): QuestionAnsweringOutputToRdfSurfacesCascUseCase {
        return QuestionAnsweringOutputToRdfSurfacesCascUseCase(
            tptpTupleAnswerModelToRdfSurfaceUseCase = createTptpTupleAnswerModelToRdfSurfaceUseCase(),
            szsParserService = createSzsParserService()
        )
    }

    fun createTptpTupleAnswerModelToRdfSurfaceUseCase(): TPTPTupleAnswerModelToRdfSurfaceUseCase {
        return TPTPTupleAnswerModelToRdfSurfaceUseCase(
            rdfSurfaceModelToN3UseCase = createRdfSurfaceModelToN3UseCase(),
            fOLGeneralTermToRDFTermUseCase = createFolGeneralTermToRdfTermUseCase()
        )
    }

    fun createFolGeneralTermToRdfTermUseCase(): FOLGeneralTermToRDFTermUseCase {
        return FOLGeneralTermToRDFTermUseCase()
    }

    fun createTransformUseCase(): TransformUseCase {
        return TransformUseCase(
            fileService = createFileService(),
            rdfSurfaceParseService = createRDFSurfaceParseService(),
            tPTPAnnotatedFormulaModelToStringUseCase = createTPTPAnnotatedFormulaModelToStringUseCase(),
            rdfSurfaceModelToTPTPModelUseCase = createRdfSurfaceModelToTPTPModelUseCase(),
        )
    }

    private fun createRdfSurfaceModelToN3UseCase(): RdfSurfaceModelToN3UseCase {
        return RdfSurfaceModelToN3UseCase(
            n3SRDFTermCoderService = createN3SRDFTermCoderService()
        )
    }

    fun createGetTheoremProverCommandUseCase(): GetTheoremProverCommandUseCase {
        return GetTheoremProverCommandUseCase(
            createConfigLoaderService(),
        )
    }


    private fun createTPTPAnnotatedFormulaModelToStringUseCase(): TPTPAnnotatedFormulaModelToStringUseCase {
        return TPTPAnnotatedFormulaModelToStringUseCase(
            fOLModelToStringUseCase = createFOLModelToStringUseCase(),
            fOLCoderService = createFOLCoderService(),
        )
    }

    private fun createFOLModelToStringUseCase(): FOLModelToStringUseCase {
        return FOLModelToStringUseCase()
    }

    private fun createRdfSurfaceModelToTPTPModelUseCase(): RdfSurfaceModelToTPTPModelUseCase {
        return RdfSurfaceModelToTPTPModelUseCase()
    }

    private fun createFileService(): FileService {
        return FileServiceImpl()
    }

    private fun createSzsParserService(): SZSParserService {
        return SZSParserServiceImpl(
            tptpTupleAnswerFormToModelService = createTptpTupleAnswerFormParserService()
        )
    }

    private fun createRDFSurfaceParseService(): RDFSurfaceParseService {
        return RDFSurfaceParseServiceImpl()
    }

    private fun createConfigLoaderService(): ConfigLoaderService {
        return ConfigLoaderServiceImpl()
    }

    private fun createTheoremProverRunnerService(): TheoremProverRunnerService {
        return TheoremProverRunnerServiceImpl()
    }

    private fun createTptpTupleAnswerFormParserService(): TptpTupleAnswerFormParserService {
        return TptpTupleAnswerFormToModelServiceImpl()
    }

    private fun createFOLCoderService(): FOLCoderService {
        return FOLCoderService()
    }

    private fun createN3SRDFTermCoderService(): N3SRDFTermCoderService {
        return N3SRDFTermCoderService()
    }

}