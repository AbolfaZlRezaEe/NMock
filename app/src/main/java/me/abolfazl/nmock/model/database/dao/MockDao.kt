package me.abolfazl.nmock.model.database.dao

import androidx.room.*
import me.abolfazl.nmock.model.database.MockType
import me.abolfazl.nmock.model.database.models.MockEntity

@Dao
interface MockDao {

    @Query("SELECT * FROM mock_table")
    suspend fun getAllMocks(): List<MockEntity>

    @Query("DELETE FROM mock_table")
    suspend fun deleteAllMocks()

    @Query("SELECT * FROM mock_table WHERE mock_type =:mockType")
    suspend fun getSpecificTypeOfMock(@MockType mockType: String): List<MockEntity>

    @Query("SELECT * FROM mock_table WHERE id =:mockId LIMIT 1")
    suspend fun getMockFromId(mockId: Long): MockEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockInformation(mockEntity: MockEntity): Long

    @Update
    suspend fun updateMockInformation(mockEntity: MockEntity)

    @Query("DELETE FROM mock_table WHERE id=:mockId")
    suspend fun deleteMockEntity(mockId: Long)
}