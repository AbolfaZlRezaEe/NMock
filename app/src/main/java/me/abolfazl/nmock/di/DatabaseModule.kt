package me.abolfazl.nmock.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.model.database.NMockDataBase
import me.abolfazl.nmock.model.database.dao.MockDao
import me.abolfazl.nmock.model.database.dao.PositionDao
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
    fun providePositionDao(
        database: NMockDataBase
    ): PositionDao = database.getPositionDao()

    @Singleton
    @Provides
    fun provideMockDao(
        database: NMockDataBase
    ): MockDao = database.getMockDao()
}