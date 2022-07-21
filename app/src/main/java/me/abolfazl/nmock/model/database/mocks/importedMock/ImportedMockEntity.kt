package me.abolfazl.nmock.model.database.mocks.importedMock

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.abolfazl.nmock.model.database.mocks.MockProvider
import me.abolfazl.nmock.model.database.mocks.MockType

@Entity(tableName = "imported_mock_table")
data class ImportedMockEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @MockType
    @ColumnInfo(name = "mock_type")
    val type: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "origin_address")
    val originAddress: String?,
    @ColumnInfo(name = "destination_address")
    val destinationAddress: String?,
    @ColumnInfo(name = "origin_location")
    val originLocation: String,
    @ColumnInfo(name = "destination_location")
    val destinationLocation: String,
    @ColumnInfo(name = "speed")
    val speed: Int,
    @ColumnInfo(name = "bearing")
    val bearing: Float,
    @ColumnInfo(name = "accuracy")
    val accuracy: Float,
    @MockProvider
    @ColumnInfo(name = "provider")
    val provider: String,
    @ColumnInfo(name = "created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,
    @ColumnInfo(name = "file_created_at")
    val fileCreatedAt: String,
    @ColumnInfo(name = "file_owner")
    val fileOwner: String,
    @ColumnInfo(name = "version_code")
    val versionCode: Int,
)
