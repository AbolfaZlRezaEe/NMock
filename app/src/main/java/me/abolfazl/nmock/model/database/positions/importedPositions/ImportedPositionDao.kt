package me.abolfazl.nmock.model.database.positions.importedPositions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImportedPositionDao {

    @Query("SELECT * FROM imported_position_table WHERE mock_id=:mockId")
    suspend fun getImportedMockPositionListFromId(mockId: Long): List<ImportedPositionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportedMockPosition(importedPositionEntity: ImportedPositionEntity): Long

    @Query("DELETE FROM imported_position_table WHERE mock_id=:mockId")
    suspend fun deleteImportedRouteInformation(mockId: Long)

}