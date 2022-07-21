package me.abolfazl.nmock.model.database.mocks.importedMock

import androidx.room.*
import me.abolfazl.nmock.model.database.mocks.MockType

@Dao
interface ImportedMockDao {

    @Query("SELECT * FROM imported_mock_table")
    suspend fun getAllImportedMocks(): List<ImportedMockEntity>

    @Query("DELETE FROM imported_mock_table")
    suspend fun deleteAllImportedMocks()

    @Query("SELECT * FROM imported_mock_table WHERE mock_type =:mockType")
    suspend fun getSpecificTypeOfImportedMock(@MockType mockType: String): List<ImportedMockEntity>

    @Query("SELECT * FROM imported_mock_table WHERE id =:mockId LIMIT 1")
    suspend fun getImportedMockFromId(mockId: Long): ImportedMockEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportedMock(importedMockEntity: ImportedMockEntity): Long

    @Update
    suspend fun updateImportedMockInformation(importedMockEntity: ImportedMockEntity)

    @Query("DELETE FROM imported_mock_table WHERE id=:mockId")
    suspend fun deleteImportedMockEntity(mockId: Long)
}