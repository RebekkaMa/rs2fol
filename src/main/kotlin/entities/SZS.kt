package entities

// Sealed classes for status categorization
sealed interface SZSStatus {
    enum class DeductiveStatus : SZSStatus {
        THEOREM, CONTRADICTION, UNSATISFIABLE, SATISFIABLE, UNKNOWN, COUNTER_SATISFIABLE, NO_CONSEQUENCE, TAUTOLOGY, OPEN
    }

    enum class PreservingStatus : SZSStatus {
        EQUIVALENT, EQUI_SATISFIABLE, SATISFIABILITY_PRESERVED, STRONGLY_SATISFIABILITY_PRESERVED
    }

    enum class UnsolvedStatus : SZSStatus {
        UNSOLVED, INCOMPLETE, RESOURCE_OUT, TIMEOUT, ERROR, GAVE_UP
    }
}

enum class OutputOntology {
    PROOF, MODEL, COUNTERMODEL, INTERPRETATION, SATISFIABLE_INTERPRETATION, REFUTATION
}

// Data class to hold SZS information
data class SZSData(
    var status: SZSStatus? = null,
    var problem: String? = null,
    val outputs: MutableMap<OutputOntology, String> = mutableMapOf()
)