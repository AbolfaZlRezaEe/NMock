package me.abolfazl.nmock.utils.response

import androidx.annotation.StringRes

data class OneTimeEmitter<E>(
    val actionId: String,
    @StringRes val message: Int,
)