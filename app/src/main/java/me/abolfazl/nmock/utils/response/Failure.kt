package me.abolfazl.nmock.utils.response

data class Failure<out E>(val error: E) : Response<Nothing, E>()