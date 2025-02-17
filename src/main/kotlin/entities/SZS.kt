package entities

import entities.fol.tptp.TPTPTupleAnswerFormAnswer

// Sealed classes for status categorization
sealed interface SZSStatusType {
    enum class SuccessOntology : SZSStatusType {
        SUCCESS, // The logical data has been processed successfully.
        UNSATISFIABILITY_PRESERVING, // If there does not exist a model of Ax then there does not exist a model of C, i.e., if Ax is unsatisfiable then C is unsatisfiable.
        SATISFIABILITY_PRESERVING, // If there exists a model of Ax then there exists a model of C, i.e., if Ax is satisfiable then C is satisfiable.
        EQUI_SATISFIABLE, // There exists a model of Ax iff there exists a model of C, i.e., Ax is (un)satisfiable iff C is (un)satisfiable.
        SATISFIABLE, // Some interpretations are models of Ax, and some models of Ax are models of C.
        FINITELY_SATISFIABLE, // Some finite interpretations are finite models of Ax, and some finite models of Ax are finite models of C.
        FINITE_THEOREM, // All finite models of Ax are finite models of C.
        THEOREM, // All models of Ax are models of C.
        SATISFIABLE_THEOREM, // Some interpretations are models of Ax, and all models of Ax are models of C.
        EQUIVALENT, // Some interpretations are models of Ax, all models of Ax are models of C, and all models of C are models of Ax.
        TAUTOLOGOUS_CONCLUSION, // Some interpretations are models of Ax, and all interpretations are models of C.
        WEAKER_CONCLUSION, // Some interpretations are models of Ax, all models of Ax are models of C, and some models of C are not models of Ax.
        EQUIVALENT_THEOREM, // Some, but not all, interpretations are models of Ax, all models of Ax are models of C, and all models of C are models of Ax.
        TAUTOLOGY, // All interpretations are models of Ax, and all interpretations are models of C.
        WEAKER_TAUTOLOGOUS_CONCLUSION, // Some, but not all, interpretations are models of Ax, and all interpretations are models of C.
        WEAKER_THEOREM, // Some interpretations are models of Ax, all models of Ax are models of C, some models of C are not models of Ax, and some interpretations are not models of C.
        CONTRADICTORY_AXIOMS, // No interpretations are models of Ax.
        SATISFIABLE_CONCLUSION_CONTRADICTORY_AXIOMS, // No interpretations are models of Ax, and some interpretations are models of C.
        TAUTOLOGOUS_CONCLUSION_CONTRADICTORY_AXIOMS, // No interpretations are models of Ax, and all interpretations are models of C.
        WEAKER_CONCLUSION_CONTRADICTORY_AXIOMS, // No interpretations are models of Ax, and some, but not all, interpretations are models of C.
        COUNTER_UNSATISFIABILITY_PRESERVING, // If there does not exist a model of Ax then there does not exist a model of ~C, i.e., if Ax is unsatisfiable then ~C is unsatisfiable.
        COUNTER_SATISFIABILITY_PRESERVING, // If there exists a model of Ax then there exists a model of ~C, i.e., if Ax is satisfiable then ~C is satisfiable.
        EQUI_COUNTER_SATISFIABLE, // There exists a model of Ax iff there exists a model of ~C, i.e., Ax is (un)satisfiable iff ~C is (un)satisfiable.
        COUNTER_SATISFIABLE, // Some interpretations are models of Ax, and some models of Ax are models of ~C.
        FINITELY_COUNTER_SATISFIABLE, // Some finite interpretations are finite models of Ax, and some finite models of Ax are finite models of ~C.
        COUNTER_THEOREM, // All models of Ax are models of ~C.
        SATISFIABLE_COUNTER_THEOREM, // Some interpretations are models of Ax, and all models of Ax are models of ~C.
        COUNTER_EQUIVALENT, // Some interpretations are models of Ax, all models of Ax are models of ~C, and all models of ~C are models of Ax.
        UNSATISFIABLE_CONCLUSION, // Some interpretations are models of Ax, and all interpretations are models of ~C.
        WEAKER_COUNTER_CONCLUSION, // Some interpretations are models of Ax, and all models of Ax are models of ~C, and some models of ~C are not models of Ax.
        EQUIVALENT_COUNTER_THEOREM, // Some, but not all, interpretations are models of Ax, all models of Ax are models of ~C, and all models of ~C are models of Ax.
        FINITELY_UNSATISFIABLE, // All finite interpretations are finite models of Ax, and all finite interpretations are finite models of ~C.
        UNSATISFIABLE, // All interpretations are models of Ax, and all interpretations are models of ~C.
        WEAKER_UNSATISFIABLE_CONCLUSION, // Some, but not all, interpretations are models of Ax, and all interpretations are models of ~C.
        WEAKER_COUNTER_THEOREM, // Some interpretations are models of Ax, all models of Ax are models of ~C, some models of ~C are not models of Ax, and some interpretations are not models of ~C.
        SATISFIABLE_COUNTER_CONCLUSION_CONTRADICTORY_AXIOMS, // No interpretations are models of Ax, and some interpretations are models of ~C.
        UNSATISFIABLE_CONCLUSION_CONTRADICTORY_AXIOMS, // No interpretations are models of Ax, and all interpretations are models of ~C.
        NO_CONSEQUENCE // Some interpretations are models of Ax, some models of Ax are models of C, and some models of Ax are models of ~C.
    }

    enum class NoSuccessOntology : SZSStatusType {
        NO_SUCCESS, // The logical data has not been processed successfully (yet).
        OPEN, // A success value has never been established.
        UNKNOWN, // Success value unknown, and no assumption has been made.
        ASSUMED, // The success ontology value S has been assumed because the actual value is unknown for the no-success ontology reason U.
        STOPPED, // Software attempted to process the data, and stopped without a success status.
        ERROR, // Software stopped due to an error.
        OS_ERROR, // Software stopped due to an operating system error.
        INPUT_ERROR, // Software stopped due to an input error.
        USAGE_ERROR, // Software stopped due to an ATP system usage error.
        SYNTAX_ERROR, // Software stopped due to an input syntax error.
        SEMANTIC_ERROR, // Software stopped due to an input semantic error.
        TYPE_ERROR, // Software stopped due to an input type error (for typed logical data).
        FORCED, // Software was forced to stop by an external force.
        USER, // Software was forced to stop by the user.
        RESOURCE_OUT, // Software stopped because some resource ran out.
        TIMEOUT, // Software stopped because the CPU time limit ran out.
        MEMORY_OUT, // Software stopped because the memory limit ran out.
        GAVE_UP, // Software gave up of its own accord.
        INCOMPLETE, // Software gave up because it's incomplete.
        INAPPROPRIATE, // Software gave up because it cannot process this type of data.
        IN_PROGRESS, // Software is still running.
        NOT_TRIED, // Software has not tried to process the data.
        NOT_TRIED_YET // Software has not tried to process the data yet, but might in the future.
    }
}


enum class SZSOutputType {
    LOGICAL_DATA, // Logical data.
    SOLUTION, // A solution.
    PROOF, // A proof.
    DERIVATION, // A derivation (inference steps ending in the theorem, in the Hilbert style).
    REFUTATION, // A refutation (starting with Ax U ~C and ending in FALSE).
    CNF_REFUTATION, // A refutation in clause normal form, including, for FOF Ax or C, the translation from FOF to CNF (without the FOF to CNF translation it's an IncompleteProof).
    INTERPRETATION, // An interpretation.
    MODEL, // A model.
    PARTIAL_INTERPRETATION, // A partial interpretation.
    PARTIAL_MODEL, // A partial model.
    STRICTLY_PARTIAL_INTERPRETATION, // A strictly partial interpretation.
    STRICTLY_PARTIAL_MODEL, // A strictly partial model.
    DOMAIN_INTERPRETATION, // An interpretation whose domain is not the Herbrand universe.
    DOMAIN_MODEL, // A model whose domain is not the Herbrand universe.
    DOMAIN_PARTIAL_INTERPRETATION, // A domain interpretation that is partial.
    DOMAIN_PARTIAL_MODEL, // A domain model that is partial.
    DOMAIN_STRICTLY_PARTIAL_INTERPRETATION, // A domain interpretation that is strictly partial.
    DOMAIN_STRICTLY_PARTIAL_MODEL, // A domain model that is strictly partial.
    FINITE_INTERPRETATION, // A domain interpretation with a finite domain.
    FINITE_MODEL, // A domain model with a finite domain.
    FINITE_PARTIAL_INTERPRETATION, // A domain partial interpretation with a finite domain.
    FINITE_PARTIAL_MODEL, // A domain partial model with a finite domain.
    FINITE_STRICTLY_PARTIAL_INTERPRETATION, // A domain strictly partial interpretation with a finite domain.
    FINITE_STRICTLY_PARTIAL_MODEL, // A domain strictly partial model with a finite domain.
    INTEGER_INTERPRETATION, // An integer domain interpretation.
    INTEGER_MODEL, // An integer domain model.
    INTEGER_PARTIAL_INTERPRETATION, // An integer domain partial interpretation.
    INTEGER_PARTIAL_MODEL, // An integer domain partial model.
    INTEGER_STRICTLY_PARTIAL_INTERPRETATION, // An integer domain strictly partial interpretation.
    INTEGER_STRICTLY_PARTIAL_MODEL, // An integer domain strictly partial model.
    REAL_INTERPRETATION, // A real domain interpretation.
    REAL_MODEL, // A real domain model.
    REAL_PARTIAL_INTERPRETATION, // A real domain partial interpretation.
    REAL_PARTIAL_MODEL, // A real domain partial model.
    REAL_STRICTLY_PARTIAL_INTERPRETATION, // A real domain strictly partial interpretation.
    REAL_STRICTLY_PARTIAL_MODEL, // A real domain strictly partial model.
    HERBRAND_INTERPRETATION, // A Herbrand interpretation.
    HERBRAND_MODEL, // A Herbrand model.
    FORMULA_INTERPRETATION, // A Herbrand interpretation defined by a set of TPTP formulae.
    FORMULA_MODEL, // A Herbrand model defined by a set of TPTP formulae.
    FORMULA_PARTIAL_INTERPRETATION, // A Herbrand partial interpretation defined by a set of TPTP formulae.
    FORMULA_PARTIAL_MODEL, // A Herbrand partial model defined by a set of TPTP formulae.
    FORMULA_STRICTLY_PARTIAL_INTERPRETATION, // A Herbrand strictly partial interpretation defined by a set of TPTP formulae.
    FORMULA_STRICTLY_PARTIAL_MODEL, // A Herbrand strictly partial model defined by a set of TPTP formulae.
    SATURATION, // A Herbrand model expressed as a saturating set of formulae.
    LIST_OF_FORMULAE, // A list of formulae.
    LIST_OF_THF, // A list of THF formulae.
    LIST_OF_TFF, // A list of TFF formulae.
    LIST_OF_FOF, // A list of FOF formulae.
    LIST_OF_CNF, // A list of CNF formulae.
    NOT_A_SOLUTION, // Something that is not a well formed solution.
    ASSURANCE, // Only an assurance of the success ontology value.
    INCOMPLETE_PROOF, // A proof with some part missing.
    INCOMPLETE_INTERPRETATION, // An interpretation with some part missing.
    NONE // Nothing.
}

enum class AnswersOntology {
    TUPLE,
    INSTANTIATED,
    INSTANTIATED_FORMULAE,
    FORMULAE,
}

interface SZSModel {
    val statusType: SZSStatusType
    val statusDetails: String?
    val identifier: String?
}

data class SZSStatus(
    override val statusType: SZSStatusType,
    override val statusDetails: String? = null,
    override val identifier: String?,
) : SZSModel


// Data class to hold SZS information
data class SZSOutputModel(
    val status: SZSStatus,
    val outputType: SZSOutputType,
    val output: List<String>,
    val outputStartDetails: String? = null,
    val outputEndDetails: String? = null,
) : SZSModel by status

data class SZSAnswerTupleFormModel(
    val status: SZSStatus,
    val tptpTupleAnswerFormAnswer: TPTPTupleAnswerFormAnswer,
    val outputStartDetails: String? = null,
    val outputEndDetails: String? = null,
) : SZSModel by status


fun String.toSZSStatusType(): SZSStatusType {
    return when (this.uppercase()) {
        "SUCCESS" -> SZSStatusType.SuccessOntology.SUCCESS
        "UNSATISFIABILITYPRESERVING" -> SZSStatusType.SuccessOntology.UNSATISFIABILITY_PRESERVING
        "SATISFIABILITYPRESERVING" -> SZSStatusType.SuccessOntology.SATISFIABILITY_PRESERVING
        "EQUISATISFIABLE" -> SZSStatusType.SuccessOntology.EQUI_SATISFIABLE
        "SATISFIABLE" -> SZSStatusType.SuccessOntology.SATISFIABLE
        "FINITELYSATISFIABLE" -> SZSStatusType.SuccessOntology.FINITELY_SATISFIABLE
        "FINITETHEOREM" -> SZSStatusType.SuccessOntology.FINITE_THEOREM
        "THEOREM" -> SZSStatusType.SuccessOntology.THEOREM
        "SATISFIABLETHEOREM" -> SZSStatusType.SuccessOntology.SATISFIABLE_THEOREM
        "EQUIVALENT" -> SZSStatusType.SuccessOntology.EQUIVALENT
        "TAUTOLOGOUSCONCLUSION" -> SZSStatusType.SuccessOntology.TAUTOLOGOUS_CONCLUSION
        "WEAKERCONCLUSION" -> SZSStatusType.SuccessOntology.WEAKER_CONCLUSION
        "EQUIVALENTTHEOREM" -> SZSStatusType.SuccessOntology.EQUIVALENT_THEOREM
        "TAUTOLOGY" -> SZSStatusType.SuccessOntology.TAUTOLOGY
        "WEAKERTAUTOLOGOUSCONCLUSION" -> SZSStatusType.SuccessOntology.WEAKER_TAUTOLOGOUS_CONCLUSION
        "WEAKERTHEOREM" -> SZSStatusType.SuccessOntology.WEAKER_THEOREM
        "CONTRADICTORYAXIOMS" -> SZSStatusType.SuccessOntology.CONTRADICTORY_AXIOMS
        "SATISFIABLECONCLUSIONCONTRADICTORYAXIOMS" -> SZSStatusType.SuccessOntology.SATISFIABLE_CONCLUSION_CONTRADICTORY_AXIOMS
        "TAUTOLOGOUSCONCLUSIONCONTRADICTORYAXIOMS" -> SZSStatusType.SuccessOntology.TAUTOLOGOUS_CONCLUSION_CONTRADICTORY_AXIOMS
        "WEAKERCONCLUSIONCONTRADICTORYAXIOMS" -> SZSStatusType.SuccessOntology.WEAKER_CONCLUSION_CONTRADICTORY_AXIOMS
        "COUNTERUNSATISFIABILITYPRESERVING" -> SZSStatusType.SuccessOntology.COUNTER_UNSATISFIABILITY_PRESERVING
        "COUNTERSATISFIABILITYPRESERVING" -> SZSStatusType.SuccessOntology.COUNTER_SATISFIABILITY_PRESERVING
        "EQUICOUNTERSATISFIABLE" -> SZSStatusType.SuccessOntology.EQUI_COUNTER_SATISFIABLE
        "COUNTERSATISFIABLE" -> SZSStatusType.SuccessOntology.COUNTER_SATISFIABLE
        "FINITELYCOUNTERSATISFIABLE" -> SZSStatusType.SuccessOntology.FINITELY_COUNTER_SATISFIABLE
        "COUNTERTHEOREM" -> SZSStatusType.SuccessOntology.COUNTER_THEOREM
        "SATISFIABLECOUNTERTHEOREM" -> SZSStatusType.SuccessOntology.SATISFIABLE_COUNTER_THEOREM
        "COUNTEREQUIVALENT" -> SZSStatusType.SuccessOntology.COUNTER_EQUIVALENT
        "UNSATISFIABLECONCLUSION" -> SZSStatusType.SuccessOntology.UNSATISFIABLE_CONCLUSION
        "WEAKERCOUNTERCONCLUSION" -> SZSStatusType.SuccessOntology.WEAKER_COUNTER_CONCLUSION
        "EQUIVALENTCOUNTERTHEOREM" -> SZSStatusType.SuccessOntology.EQUIVALENT_COUNTER_THEOREM
        "FINITELYUNSATISFIABLE" -> SZSStatusType.SuccessOntology.FINITELY_UNSATISFIABLE
        "UNSATISFIABLE" -> SZSStatusType.SuccessOntology.UNSATISFIABLE
        "WEAKERUNSATISFIABLECONCLUSION" -> SZSStatusType.SuccessOntology.WEAKER_UNSATISFIABLE_CONCLUSION
        "WEAKERCOUNTERTHEOREM" -> SZSStatusType.SuccessOntology.WEAKER_COUNTER_THEOREM
        "SATISFIABLECOUNTERCONCLUSIONCONTRADICTORYAXIOMS" -> SZSStatusType.SuccessOntology.SATISFIABLE_COUNTER_CONCLUSION_CONTRADICTORY_AXIOMS
        "UNSATISFIABLECONCLUSIONCONTRADICTORYAXIOMS" -> SZSStatusType.SuccessOntology.UNSATISFIABLE_CONCLUSION_CONTRADICTORY_AXIOMS
        "NOCONSEQUENCE" -> SZSStatusType.SuccessOntology.NO_CONSEQUENCE
        "NOSUCCESS" -> SZSStatusType.NoSuccessOntology.NO_SUCCESS
        "OPEN" -> SZSStatusType.NoSuccessOntology.OPEN
        "UNKNOWN" -> SZSStatusType.NoSuccessOntology.UNKNOWN
        "ASSUMED" -> SZSStatusType.NoSuccessOntology.ASSUMED
        "STOPPED" -> SZSStatusType.NoSuccessOntology.STOPPED
        "ERROR" -> SZSStatusType.NoSuccessOntology.ERROR
        "OSERROR" -> SZSStatusType.NoSuccessOntology.OS_ERROR
        "INPUTERROR" -> SZSStatusType.NoSuccessOntology.INPUT_ERROR
        "USAGEERROR" -> SZSStatusType.NoSuccessOntology.USAGE_ERROR
        "SYNTAXERROR" -> SZSStatusType.NoSuccessOntology.SYNTAX_ERROR
        "SEMANTICERROR" -> SZSStatusType.NoSuccessOntology.SEMANTIC_ERROR
        "TYPEERROR" -> SZSStatusType.NoSuccessOntology.TYPE_ERROR
        "FORCED" -> SZSStatusType.NoSuccessOntology.FORCED
        "USER" -> SZSStatusType.NoSuccessOntology.USER
        "RESOURCEOUT" -> SZSStatusType.NoSuccessOntology.RESOURCE_OUT
        "TIMEOUT" -> SZSStatusType.NoSuccessOntology.TIMEOUT
        "MEMORYOUT" -> SZSStatusType.NoSuccessOntology.MEMORY_OUT
        "GAVEUP" -> SZSStatusType.NoSuccessOntology.GAVE_UP
        "INCOMPLETE" -> SZSStatusType.NoSuccessOntology.INCOMPLETE
        "INAPPROPRIATE" -> SZSStatusType.NoSuccessOntology.INAPPROPRIATE
        "INPROGRESS" -> SZSStatusType.NoSuccessOntology.IN_PROGRESS
        "NOTTRIED" -> SZSStatusType.NoSuccessOntology.NOT_TRIED
        "NOTTRIEDYET" -> SZSStatusType.NoSuccessOntology.NOT_TRIED_YET
        else -> throw IllegalArgumentException("Invalid SZSStatusType")
    }
}

fun String.toSZSOutputType(): SZSOutputType {
    return when (this.uppercase()) {
        "LOGICALDATA" -> SZSOutputType.LOGICAL_DATA
        "SOLUTION" -> SZSOutputType.SOLUTION
        "PROOF" -> SZSOutputType.PROOF
        "DERIVATION" -> SZSOutputType.DERIVATION
        "REFUTATION" -> SZSOutputType.REFUTATION
        "CNFREFUTATION" -> SZSOutputType.CNF_REFUTATION
        "INTERPRETATION" -> SZSOutputType.INTERPRETATION
        "MODEL" -> SZSOutputType.MODEL
        "PARTIALINTERPRETATION" -> SZSOutputType.PARTIAL_INTERPRETATION
        "PARTIALMODEL" -> SZSOutputType.PARTIAL_MODEL
        "STRICTLYPARTIALINTERPRETATION" -> SZSOutputType.STRICTLY_PARTIAL_INTERPRETATION
        "STRICTLYPARTIALMODEL" -> SZSOutputType.STRICTLY_PARTIAL_MODEL
        "DOMAININTERPRETATION" -> SZSOutputType.DOMAIN_INTERPRETATION
        "DOMAINMODEL" -> SZSOutputType.DOMAIN_MODEL
        "DOMAINPARTIALINTERPRETATION" -> SZSOutputType.DOMAIN_PARTIAL_INTERPRETATION
        "DOMAINPARTIALMODEL" -> SZSOutputType.DOMAIN_PARTIAL_MODEL
        "DOMAINSTRICTLYPARTIALINTERPRETATION" -> SZSOutputType.DOMAIN_STRICTLY_PARTIAL_INTERPRETATION
        "DOMAINSTRICTLYPARTIALMODEL" -> SZSOutputType.DOMAIN_STRICTLY_PARTIAL_MODEL
        "FINITEINTERPRETATION" -> SZSOutputType.FINITE_INTERPRETATION
        "FINITEMODEL" -> SZSOutputType.FINITE_MODEL
        "FINITEPARTIALINTERPRETATION" -> SZSOutputType.FINITE_PARTIAL_INTERPRETATION
        "FINITEPARTIALMODEL" -> SZSOutputType.FINITE_PARTIAL_MODEL
        "FINITESTRICTLYPARTIALINTERPRETATION" -> SZSOutputType.FINITE_STRICTLY_PARTIAL_INTERPRETATION
        "FINITESTRICTLYPARTIALMODEL" -> SZSOutputType.FINITE_STRICTLY_PARTIAL_MODEL
        "INTEGERINTERPRETATION" -> SZSOutputType.INTEGER_INTERPRETATION
        "INTEGERMODEL" -> SZSOutputType.INTEGER_MODEL
        "INTEGERPARTIALINTERPRETATION" -> SZSOutputType.INTEGER_PARTIAL_INTERPRETATION
        "INTEGERPARTIALMODEL" -> SZSOutputType.INTEGER_PARTIAL_MODEL
        "INTEGERSTRICTLYPARTIALINTERPRETATION" -> SZSOutputType.INTEGER_STRICTLY_PARTIAL_INTERPRETATION
        "INTEGERSSTRICTLYPARTIALMODEL" -> SZSOutputType.INTEGER_STRICTLY_PARTIAL_MODEL
        "REALINTERPRETATION" -> SZSOutputType.REAL_INTERPRETATION
        "REALMODEL" -> SZSOutputType.REAL_MODEL
        "REALPARTIALINTERPRETATION" -> SZSOutputType.REAL_PARTIAL_INTERPRETATION
        "REALPARTIALMODEL" -> SZSOutputType.REAL_PARTIAL_MODEL
        "REALSTRICTLYPARTIALINTERPRETATION" -> SZSOutputType.REAL_STRICTLY_PARTIAL_INTERPRETATION
        "REALSTRICTLYPARTIALMODEL" -> SZSOutputType.REAL_STRICTLY_PARTIAL_MODEL
        "HERBRANDINTERPRETATION" -> SZSOutputType.HERBRAND_INTERPRETATION
        "HERBRANDMODEL" -> SZSOutputType.HERBRAND_MODEL
        "FORMULAINTERPRETATION" -> SZSOutputType.FORMULA_INTERPRETATION
        "FORMULAMODEL" -> SZSOutputType.FORMULA_MODEL
        "FORMULAPARTIALINTERPRETATION" -> SZSOutputType.FORMULA_PARTIAL_INTERPRETATION
        "FORMULAPARTIALMODEL" -> SZSOutputType.FORMULA_PARTIAL_MODEL
        "FORMULASTRICTLYPARTIALINTERPRETATION" -> SZSOutputType.FORMULA_STRICTLY_PARTIAL_INTERPRETATION
        "FORMULASTRICTLYPARTIALMODEL" -> SZSOutputType.FORMULA_STRICTLY_PARTIAL_MODEL
        "SATURATION" -> SZSOutputType.SATURATION
        "LISTOFFORMULAE" -> SZSOutputType.LIST_OF_FORMULAE
        "LISTOFTHF" -> SZSOutputType.LIST_OF_THF
        "LISTOFTFF" -> SZSOutputType.LIST_OF_TFF
        "LISTOFFOF" -> SZSOutputType.LIST_OF_FOF
        "LISTOFCNF" -> SZSOutputType.LIST_OF_CNF
        "NOTASOLUTION" -> SZSOutputType.NOT_A_SOLUTION
        "ASSURANCE" -> SZSOutputType.ASSURANCE
        "INCOMPLETEPROOF" -> SZSOutputType.INCOMPLETE_PROOF
        "INCOMPLETEINTERPRETATION" -> SZSOutputType.INCOMPLETE_INTERPRETATION
        "NONE" -> SZSOutputType.NONE
        else -> throw IllegalArgumentException("Invalid SZSOutputType")
    }
}

fun String.toSZSAnswersOntology(): AnswersOntology {
    return when (this.uppercase()) {
        "TUPLE" -> AnswersOntology.TUPLE
        "INSTANTIATED" -> AnswersOntology.INSTANTIATED
        "INSTANTIATEDFORMULAE" -> AnswersOntology.INSTANTIATED_FORMULAE
        "FORMULAE" -> AnswersOntology.FORMULAE
        else -> throw IllegalArgumentException("Invalid AnswersOntology")
    }
}
