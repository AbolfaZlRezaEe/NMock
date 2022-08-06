package me.abolfazl.nmock.model.database.mocks

import androidx.annotation.StringDef

const val TYPE_CUSTOM_CREATION = "CUSTOM_CREATION"
const val TYPE_AUTOMATIC_CREATION = "AUTOMATIC_CREATION"

@StringDef(TYPE_CUSTOM_CREATION, TYPE_AUTOMATIC_CREATION)
annotation class MockCreationType