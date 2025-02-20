package entities.fol

sealed class GeneralTerm : FOLExpression()

data class FOLVariable(val name: String) : GeneralTerm()
data class FOLFunction(val name: String, val arguments: List<GeneralTerm>) : GeneralTerm()
open class FOLConstant(val name: String) : GeneralTerm() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FOLConstant) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }
}

object FOLTrue : FOLConstant("true")
object FOLFalse : FOLConstant("false")


