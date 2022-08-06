package me.abolfazl.nmock.model.database.mocks.normalMock

import androidx.room.*
import me.abolfazl.nmock.model.database.mocks.MockCreationType

@Dao
interface NormalMockDao {

    @Query("SELECT * FROM mock_table")
    suspend fun getAllMocks(): List<NormalMockEntity>

    @Query("DELETE FROM mock_table")
    suspend fun deleteAllMocks()

    @Query("SELECT * FROM mock_table WHERE mock_type =:mockCreationType")
    suspend fun getSpecificTypeOfMock(@MockCreationType mockCreationType: String): List<NormalMockEntity>

    @Query("SELECT * FROM mock_table WHERE id =:mockId LIMIT 1")
    suspend fun getMockFromId(mockId: Long): NormalMockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockInformation(normalMockEntity: NormalMockEntity): Long

    @Update
    suspend fun updateMockInformation(normalMockEntity: NormalMockEntity)

    @Query("DELETE FROM mock_table WHERE id=:mockId")
    suspend fun deleteMockEntity(mockId: Long)
}