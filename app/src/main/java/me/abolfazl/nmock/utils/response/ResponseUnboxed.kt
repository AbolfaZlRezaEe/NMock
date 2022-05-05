package me.abolfazl.nmock.utils.response

class ResponseUnboxed<V, E> {

    internal var success: (V) -> Unit = {}
    internal var failure: (E) -> Unit = {}

    fun success(block: (V) -> Unit) {
        success = block
    }

    fun failure(block: (E) -> Unit) {
        failure = block
    }
}