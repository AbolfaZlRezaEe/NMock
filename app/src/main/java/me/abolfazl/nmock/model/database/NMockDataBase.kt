package me.abolfazl.nmock.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import me.abolfazl.nmock.model.database.mocks.importedMock.ImportedMockDao
import me.abolfazl.nmock.model.database.mocks.importedMock.ImportedMockEntity
import me.abolfazl.nmock.model.database.mocks.normalMock.NormalMockDao
import me.abolfazl.nmock.model.database.mocks.normalMock.NormalMockEntity
import me.abolfazl.nmock.model.database.positions.importedPositions.ImportedPositionDao
import me.abolfazl.nmock.model.database.positions.importedPositions.ImportedPositionEntity
import me.abolfazl.nmock.model.database.positions.normalPositions.NormalPositionDao
import me.abolfazl.nmock.model.database.positions.normalPositions.NormalPositionEntity

@Database(
    entities = [
        NormalMockEntity::class,
        NormalPositionEntity::class,
        ImportedMockEntity::class,
        ImportedPositionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NMockDataBase : RoomDatabase() {

    abstract fun getNormalMockDao(): NormalMockDao

    abstract fun getImportedMockDao(): ImportedMockDao

    abstract fun getNormalPositionDao(): NormalPositionDao

    abstract fun getImportedPositionDao(): ImportedPositionDao
}