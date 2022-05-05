package me.abolfazl.nmock.model.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.abolfazl.nmock.model.database.MockType
import me.abolfazl.nmock.utils.Constant

@Entity(tableName = "mock_table")
data class MockEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @ColumnInfo(name = "mock_type")
    @MockType
    val mockType: String = Constant.TYPE_CUSTOM_CREATE,
    @ColumnInfo(name = "mock_name")
    val mockName: String,
    @ColumnInfo(name = "description")
    val description: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)