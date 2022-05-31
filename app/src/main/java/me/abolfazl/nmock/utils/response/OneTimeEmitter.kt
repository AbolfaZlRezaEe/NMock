package me.abolfazl.nmock.utils.response

import androidx.annotation.StringRes

data class OneTimeEmitter(
    val actionId: String,
    @StringRes val message: Int,
)