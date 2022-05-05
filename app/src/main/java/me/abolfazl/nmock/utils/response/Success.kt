package me.abolfazl.nmock.utils.response

data class Success<out V>(val value: V) : Response<V, Nothing>()