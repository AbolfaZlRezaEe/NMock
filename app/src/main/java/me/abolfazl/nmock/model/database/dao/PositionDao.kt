package me.abolfazl.nmock.model.database.dao

import androidx.room.*
import me.abolfazl.nmock.model.database.models.PositionEntity

@Dao
interface PositionDao {

    @Query("SELECT * FROM position_table WHERE mock_id=:mockId")
    suspend fun getMockPositionListFromId(mockId: Long): List<PositionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockPosition(positionEntity: PositionEntity): Long

    @Update
    suspend fun updateMockPosition(positionEntity: PositionEntity)

    @Query("DELETE FROM position_table WHERE mock_id=:mockId")
    suspend fun deleteRouteInformation(mockId: Long)

}