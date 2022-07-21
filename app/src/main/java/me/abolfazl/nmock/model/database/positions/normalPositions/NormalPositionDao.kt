package me.abolfazl.nmock.model.database.positions.normalPositions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NormalPositionDao {

    @Query("SELECT * FROM position_table WHERE mock_id=:mockId")
    suspend fun getMockPositionListFromId(mockId: Long): List<NormalPositionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockPosition(normalPositionEntity: NormalPositionEntity): Long

    @Query("DELETE FROM position_table WHERE mock_id=:mockId")
    suspend fun deleteRouteInformation(mockId: Long)

}