package util.commandResult


typealias RootError = Error

sealed interface CommandStatus<out D, out E : RootError> {
    data class Result<out D, out E : RootError>(val data: D) : CommandStatus<D, E>
    data class Error<out D, out E : RootError>(val error: E) : CommandStatus<D, E>
    data class Info<out D, out E : RootError>(val message: String) : CommandStatus<D, E>, util.commandResult.Info

    val isResult: Boolean get() = this is Result
    val isFailure: Boolean get() = this is RootError
    val isInfo: Boolean get() = this is Info
}

sealed interface IntermediateStatus<out D, out E : RootError> {
    data class Result<out D, out E : RootError>(val data: D) : IntermediateStatus<D, E>
    data class Error<out D, out E : RootError>(val error: E) : IntermediateStatus<D, E>

    val isResult: Boolean get() = this is Result
    val isFailure: Boolean get() = this is RootError
}

inline fun <D, E : RootError, R> CommandStatus<D, E>.fold(
    onInfo: (message: String) -> R,
    onSuccess: (data: D) -> R,
    onFailure: (error: E) -> R
): R {
    return when (this) {
        is CommandStatus.Result -> onSuccess(data)
        is CommandStatus.Error -> onFailure(error)
        is CommandStatus.Info -> onInfo(message)
    }
}

inline fun <D, E : RootError, R> IntermediateStatus<D, E>.fold(
    onSuccess: (data: D) -> R,
    onFailure: (error: E) -> R
): R {
    return when (this) {
        is IntermediateStatus.Result -> onSuccess(data)
        is IntermediateStatus.Error -> onFailure(error)
    }
}

fun <D, E : RootError> CommandStatus<D, E>.getSuccessOrNull(): D? {
    return when (this) {
        is CommandStatus.Result -> data
        else -> null
    }
}

fun <D, E : RootError> IntermediateStatus<D, E>.getSuccessOrNull(): D? {
    return when (this) {
        is IntermediateStatus.Result -> data
        else -> null
    }
}

fun <D, E : RootError> CommandStatus<D, E>.getErrorOrNull(): E? {
    return when (this) {
        is CommandStatus.Error -> error
        else -> null
    }
}

fun <D, E : RootError> IntermediateStatus<D, E>.getErrorOrNull(): E? {
    return when (this) {
        is IntermediateStatus.Error -> error
        else -> null
    }
}

inline fun <D, E : RootError, R> IntermediateStatus<D, E>.map(transform: (data: D) -> R): IntermediateStatus<R, E> {
    return when (this) {
        is IntermediateStatus.Result -> IntermediateStatus.Result(transform(data))
        is IntermediateStatus.Error -> IntermediateStatus.Error(error)
    }
}

inline fun <D, E : RootError, F : RootError> IntermediateStatus<D, E>.mapError(transform: (error: E) -> IntermediateStatus.Error<D, F>): IntermediateStatus<D, F> {
    return when (this) {
        is IntermediateStatus.Result -> IntermediateStatus.Result(this.data)
        is IntermediateStatus.Error -> transform(error)
    }
}

inline fun <D, E : RootError> IntermediateStatus<D, E>.getOrElse(transform: (error: E) -> D): D {
    return when (this) {
        is IntermediateStatus.Result -> data
        is IntermediateStatus.Error -> transform(error)
    }
}

inline fun <D, E : G, F : G, G : RootError, H> IntermediateStatus<D, E>.runOnSuccess(transform: (value: D) -> IntermediateStatus<H, F>): IntermediateStatus<H, G> {
    return when (this) {
        is IntermediateStatus.Result -> transform(this.data)
        is IntermediateStatus.Error -> IntermediateStatus.Error(this.error)
    }
}

fun <D, A : RootError> intermediateError(error: A): IntermediateStatus<D, RootError> = IntermediateStatus.Error(error)
fun <A, D : RootError> intermediateSuccess(data: A): IntermediateStatus<A, D> = IntermediateStatus.Result(data)

fun <D, A : RootError> error(error: A): CommandStatus<D, RootError> = CommandStatus.Error(error)
fun <A, D : RootError> success(data: A): CommandStatus<A, D> = CommandStatus.Result(data)
fun <A, D : RootError> info(message: String): CommandStatus<A, D> = CommandStatus.Info(message)