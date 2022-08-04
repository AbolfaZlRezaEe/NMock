package me.abolfazl.nmock.model.database.positions.normalPositions

import androidx.room.*

@Dao
interface NormalPositionDao {

    @Query("SELECT * FROM position_table WHERE mock_id=:mockId")
    suspend fun getMockPositionListFromId(mockId: Long): List<NormalPositionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockPosition(normalPositionEntity: NormalPositionEntity): Long

    @Update
    suspend fun updateMockPosition(normalPositionEntity: NormalPositionEntity): Int

    @Query("DELETE FROM position_table WHERE mock_id=:mockId")
    suspend fun deleteRouteInformation(mockId: Long)

}