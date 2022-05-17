package me.abolfazl.nmock.utils.response

data class OneTimeEmitter<E>(
    val exception: E? = null,
    val message: String? = null,
)