package me.abolfazl.nmock.utils.response

fun <V, E> Response<V, E>.isSuccessful(): Boolean {
    return this is Success
}

inline infix fun <E> IsSuccessful<E>?.otherwise(block: (E) -> Unit) {
    if (this != null) {
        block.invoke(error)
    }
}

inline fun <V, E> Response<V, E>.ifSuccessful(success: (V) -> Unit): IsSuccessful<E>? {
    return when (this) {
        is Success -> {
            success.invoke(this.value)
            null
        }
        is Failure -> {
            this
        }
    }
}

inline fun <V, E> Response<V, E>.ifNotSuccessful(failure: (E) -> Unit) {
    if (this is Failure) {
        failure.invoke(this.error)
    }
}

fun <V, E> Response<V, E>.getSuccessResponse(): Success<V> {
    if (this is Success) {
        return this
    } else {
        throw IllegalStateException("The selected response is a failure.")
    }
}

fun <V, E> Response<V, E>.getFailureResponse(): Failure<E> {
    if (this is Failure) {
        return this
    } else {
        throw IllegalStateException("The selected response is a success.")
    }
}

fun <V, E> Response<V, E>.unBox(block: ResponseUnboxed<V, E>.() -> Unit) {
    val unboxed = ResponseUnboxed<V, E>().apply(block)
    when (this) {
        is Success -> unboxed.success.invoke(this.value)
        is Failure -> unboxed.failure.invoke(this.error)
    }
}

typealias IsSuccessful<E> = Failure<E>