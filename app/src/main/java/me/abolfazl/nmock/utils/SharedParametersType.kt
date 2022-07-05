package me.abolfazl.nmock.utils

import androidx.annotation.StringDef

const val SHARED_MOCK_ID = "KEY_MOCK_ID"
const val SHARED_MOCK_SETTING = "KEY_MOCK_SETTING"
const val SHARED_LOG_TIME = "KEY_LOG_TIME"
const val SHARED_LOG_CODE = "KEY_LOG_CODE"
const val SHARED_FIREBASE_TOKEN = "KEY_FIREBASE_TOKEN"
const val SHARED_AUTH_TOKEN = "KEY_AUTH_TOKEN"

@StringDef(
    SHARED_MOCK_ID,
    SHARED_MOCK_SETTING,
    SHARED_LOG_TIME,
    SHARED_LOG_CODE,
    SHARED_FIREBASE_TOKEN,
    SHARED_AUTH_TOKEN
)
annotation class SharedParametersType