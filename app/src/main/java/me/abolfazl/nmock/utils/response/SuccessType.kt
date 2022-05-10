package me.abolfazl.nmock.utils.response

import androidx.annotation.StringDef

const val SUCCESS_TYPE_MOCK_INSERTION = "MOCK_INSERTED_SUCCESSFULLY"

@StringDef(SUCCESS_TYPE_MOCK_INSERTION)
annotation class SuccessType()
