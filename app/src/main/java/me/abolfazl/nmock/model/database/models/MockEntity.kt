package me.abolfazl.nmock.model.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.model.database.MockType
import me.abolfazl.nmock.utils.Constant

@Entity(tableName = "mock_table")
data class MockEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @ColumnInfo(name = "mock_type")
    @MockType
    val type: String = Constant.TYPE_CUSTOM_CREATE,
    @ColumnInfo(name = "mock_name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "origin_location")
    val originLocation: String,
    @ColumnInfo(name = "destination_location")
    val destinationLocation: String,
    @ColumnInfo(name = "origin_address")
    val originAddress: String,
    @ColumnInfo(name = "destination_address")
    val destinationAddress: String,
    @ColumnInfo(name = "speed")
    val speed: Int,
    @ColumnInfo(name = "bearing")
    val bearing: Float,
    @ColumnInfo(name = "accuracy")
    val accuracy: Float,
    @ColumnInfo(name = "provider")
    @MockProvider
    val provider: String,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)