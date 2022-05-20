package me.abolfazl.nmock.utils

import androidx.annotation.StringDef

const val SHARED_MOCK_ID = "KEY_MOCK_ID"
const val SHARED_MOCK_SETTING = "KEY_MOCK_SETTING"

@StringDef(SHARED_MOCK_ID, SHARED_MOCK_SETTING)
annotation class SharedParametersType {
}