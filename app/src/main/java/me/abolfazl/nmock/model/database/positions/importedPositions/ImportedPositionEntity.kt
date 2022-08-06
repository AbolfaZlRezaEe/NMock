package me.abolfazl.nmock.model.database.positions.importedPositions

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "imported_position_table",
    foreignKeys = [ForeignKey(
        entity = ImportedPositionEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("mock_id"),
        onDelete = ForeignKey.CASCADE,
    )]
)
data class ImportedPositionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @ColumnInfo(name = "mock_id", index = true)
    val mockId: Long,
    @ColumnInfo(name = "lat")
    val latitude: Double,
    @ColumnInfo(name = "lng")
    val longitude: Double,
    @ColumnInfo(name = "time")
    val time: Long,
    @ColumnInfo(name = "elapsed_real_time")
    val elapsedRealTime: Long
)
