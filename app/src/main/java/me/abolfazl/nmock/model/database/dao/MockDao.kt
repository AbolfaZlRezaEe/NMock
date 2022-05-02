package me.abolfazl.nmock.model.database.dao

import androidx.room.*
import me.abolfazl.nmock.model.database.MockType
import me.abolfazl.nmock.model.database.models.MockEntity

@Dao
interface MockDao {

    @Query("SELECT * FROM mock_table")
    suspend fun getAllMocks(): List<MockEntity>

    @Query("SELECT * FROM mock_table WHERE mock_type =:mockType")
    suspend fun getSpecificTypeOfMock(@MockType mockType: String): List<MockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockInformation(mockEntity: MockEntity): Long

    @Update
    suspend fun updateMockInformation(mockEntity: MockEntity)

    @Delete
    suspend fun deleteMockEntity(mockEntity: MockEntity)
}