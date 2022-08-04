package me.abolfazl.nmock.model.database

import androidx.annotation.StringDef

const val DATABASE_TYPE_ALL = "ALL_MOCK_INFORMATION"
const val DATABASE_TYPE_NORMAL = "NORMAL_MOCK_INFORMATION"
const val DATABASE_TYPE_IMPORTED = "IMPORTED_MOCK_INFORMATION"

@StringDef(DATABASE_TYPE_IMPORTED, DATABASE_TYPE_NORMAL, DATABASE_TYPE_ALL)
annotation class MockDatabaseType