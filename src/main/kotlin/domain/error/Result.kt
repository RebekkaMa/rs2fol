package domain.error


typealias RootError = Error

sealed interface Result<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D) : Result<D, E>
    data class Error<out D, out E : RootError>(val error: E) : Result<D, E>

    public val isSuccess: Boolean get() = this is Success
    public val isFailure: Boolean get() = this is domain.error.Error
}


public inline fun <D, E : RootError, R> Result<D, E>.fold(
    onSuccess: (data: D) -> R,
    onFailure: (error: E) -> R
): R {
    return when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onFailure(error)
    }
}

public inline fun <D, E : RootError> Result<D, E>.getOrNull(): D? {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }
}

public inline fun <D, E : RootError, R> Result<D, E>.map(transform: (data: D) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(error)
    }
}

public inline fun <D, E : RootError, F : RootError> Result<D, E>.mapError(transform: (error: E) -> Result.Error<D,F>): Result<D, F> {
    return when (this) {
        is Result.Success -> Result.Success(this.data)
        is Result.Error -> transform(error)
    }
}

public inline fun <D, E : RootError> Result<D, E>.getOrElse(transform: (error: E) -> D): D {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> transform(error)
    }
}

public inline fun <D, E : G, F : G, G : RootError, H> Result<D, E>.runOnSuccess(transform: (value: D) -> Result<H, F>): Result<H, G> {
    return when(this) {
        is Result.Success -> transform(this.data)
        is Result.Error -> Result.Error(this.error)
    }
}



fun <D, A : RootError> error(error: A): Result<D, RootError> = Result.Error(error)
fun <A, D: RootError> success(data: A): Result<A, D> = Result.Success(data)