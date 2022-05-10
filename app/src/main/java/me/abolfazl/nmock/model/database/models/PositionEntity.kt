package me.abolfazl.nmock.model.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.model.database.MockType

@Entity(
    tableName = "position_table",
    foreignKeys = [ForeignKey(
        entity = MockEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("mock_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class PositionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @ColumnInfo(name = "mock_id")
    val mockId: Long,
    @ColumnInfo(name = "lat")
    val latitude: Double,
    @ColumnInfo(name = "lng")
    val longitude: Double,
    @ColumnInfo(name = "speed")
    val speed: Float,
    @ColumnInfo(name = "time")
    val time: Long,
    @ColumnInfo(name = "elapsed_real_time")
    val elapsedRealTime: Long,
    @ColumnInfo(name = "bearing")
    val bearing: Float,
    @ColumnInfo(name = "accuracy")
    val accuracy: Float,
    @ColumnInfo(name = "provider")
    @MockProvider
    val provider: String,
)
