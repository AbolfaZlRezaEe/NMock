package me.abolfazl.nmock.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.model.database.NMockDataBase
import me.abolfazl.nmock.model.database.mocks.importedMock.ImportedMockDao
import me.abolfazl.nmock.model.database.mocks.normalMock.NormalMockDao
import me.abolfazl.nmock.model.database.positions.importedPositions.ImportedPositionDao
import me.abolfazl.nmock.model.database.positions.normalPositions.NormalPositionDao
import me.abolfazl.nmock.utils.Constant
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): NMockDataBase {
        return Room.databaseBuilder(
            context,
            NMockDataBase::class.java,
            Constant.DATABASE_NAME
        ).build()
    }

    @Singleton
    @Provides
    fun provideNormalPositionDao(
        database: NMockDataBase
    ): NormalPositionDao = database.getNormalPositionDao()

    @Singleton
    @Provides
    fun provideImportedPositionDao(
        database: NMockDataBase
    ): ImportedPositionDao = database.getImportedPositionDao()

    @Singleton
    @Provides
    fun provideNormalMockDao(
        database: NMockDataBase
    ): NormalMockDao = database.getNormalMockDao()

    @Singleton
    @Provides
    fun provideImportedMockDao(
        database: NMockDataBase
    ): ImportedMockDao = database.getImportedMockDao()
}