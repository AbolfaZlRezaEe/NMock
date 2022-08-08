package me.abolfazl.nmock.model.database.mocks.normalMock

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.abolfazl.nmock.model.database.mocks.MockCreationType
import me.abolfazl.nmock.model.database.mocks.MockProvider
import me.abolfazl.nmock.model.database.mocks.TYPE_CUSTOM_CREATION

@Entity(tableName = "mock_table")
data class NormalMockEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @ColumnInfo(name = "mock_type")
    @MockCreationType
    val creationType: String = TYPE_CUSTOM_CREATION,
    @ColumnInfo(name = "mock_name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "origin_location")
    val originLocation: String,
    @ColumnInfo(name = "destination_location")
    val destinationLocation: String,
    @ColumnInfo(name = "origin_address")
    val originAddress: String?,
    @ColumnInfo(name = "destination_address")
    val destinationAddress: String?,
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