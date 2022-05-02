package me.abolfazl.nmock.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import me.abolfazl.nmock.model.database.dao.MockDao
import me.abolfazl.nmock.model.database.dao.PositionDao
import me.abolfazl.nmock.model.database.models.MockEntity
import me.abolfazl.nmock.model.database.models.PositionEntity

@Database(
    entities = [MockEntity::class, PositionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NMockDataBase : RoomDatabase() {

    abstract fun getMockDao(): MockDao
    abstract fun getPositionDao(): PositionDao
}