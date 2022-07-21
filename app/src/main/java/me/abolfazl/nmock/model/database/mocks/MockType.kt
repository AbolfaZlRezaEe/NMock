package me.abolfazl.nmock.model.database.mocks

import androidx.annotation.StringDef
import me.abolfazl.nmock.utils.Constant.TYPE_AUTOMATIC_CREATE
import me.abolfazl.nmock.utils.Constant.TYPE_CUSTOM_CREATE

@StringDef(TYPE_CUSTOM_CREATE, TYPE_AUTOMATIC_CREATE)
annotation class MockType