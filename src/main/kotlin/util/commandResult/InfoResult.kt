package util.commandResult

typealias RootError = Error

sealed interface InfoResult<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D) : InfoResult<D, E>
    data class Error<out D, out E : RootError>(val error: E) : InfoResult<D, E>
    data class Info<out D, out E : RootError>(val message: String) : InfoResult<D, E>, util.commandResult.Info

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is RootError
    val isInfo: Boolean get() = this is Info
}

sealed interface Result<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D) : Result<D, E>
    data class Error<out D, out E : RootError>(val error: E) : Result<D, E>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is RootError
}

inline fun <D, E : RootError, R> InfoResult<D, E>.fold(
    onInfo: (message: String) -> R,
    onSuccess: (data: D) -> R,
    onFailure: (error: E) -> R
): R {
    return when (this) {
        is InfoResult.Success -> onSuccess(data)
        is InfoResult.Error -> onFailure(error)
        is InfoResult.Info -> onInfo(message)
    }
}

inline fun <D, E : RootError, R> Result<D, E>.fold(
    onSuccess: (data: D) -> R,
    onFailure: (error: E) -> R
): R {
    return when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onFailure(error)
    }
}

fun <D, E : RootError> InfoResult<D, E>.getSuccessOrNull(): D? {
    return when (this) {
        is InfoResult.Success -> data
        else -> null
    }
}

fun <D, E : RootError> Result<D, E>.getSuccessOrNull(): D? {
    return when (this) {
        is Result.Success -> data
        else -> null
    }
}

fun <D, E : RootError> InfoResult<D, E>.getErrorOrNull(): E? {
    return when (this) {
        is InfoResult.Error -> error
        else -> null
    }
}

fun <D, E : RootError> Result<D, E>.getErrorOrNull(): E? {
    return when (this) {
        is Result.Error -> error
        else -> null
    }
}

inline fun <D, E : RootError, R> Result<D, E>.map(transform: (data: D) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(error)
    }
}

inline fun <D, E : RootError, F : RootError> Result<D, E>.mapError(transform: (error: E) -> Result.Error<D, F>): Result<D, F> {
    return when (this) {
        is Result.Success -> Result.Success(this.data)
        is Result.Error -> transform(error)
    }
}

inline fun <D, E : RootError> Result<D, E>.getOrElse(transform: (error: E) -> D): D {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> transform(error)
    }
}

inline fun <D, E : G, F : G, G : RootError, H> Result<D, E>.runOnSuccess(transform: (value: D) -> Result<H, F>): Result<H, G> {
    return when (this) {
        is Result.Success -> transform(this.data)
        is Result.Error -> Result.Error(this.error)
    }
}

fun <D, A : RootError> error(error: A): Result<D, RootError> = Result.Error(error)
fun <A, D : RootError> success(data: A): Result<A, D> = Result.Success(data)

fun <D, A : RootError> infoError(error: A): InfoResult<D, RootError> = InfoResult.Error(error)
fun <A, D : RootError> infoSuccess(data: A): InfoResult<A, D> = InfoResult.Success(data)
fun <A, D : RootError> infoInfo(message: String): InfoResult<A, D> = InfoResult.Info(message)