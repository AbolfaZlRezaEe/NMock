package me.abolfazl.nmock.utils.response.exceptions

import me.abolfazl.nmock.utils.response.Failure

object ExceptionMapper {

    fun map(httpCode: Int): String = when (httpCode) {
        // todo: if these exception thrown, we must capture that and check the problem!
        470 -> EXCEPTION_COORDINATE_PARSE_ERROR
        480 -> EXCEPTION_API_KEY_ERROR
        481, 482 -> EXCEPTION_LIMIT_EXCEEDED
        else -> EXCEPTION_UNKNOWN
    }

}