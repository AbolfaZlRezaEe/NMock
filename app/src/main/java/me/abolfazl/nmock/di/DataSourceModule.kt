package me.abolfazl.nmock.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.dataSource.mock.ImportedMockDataSource
import me.abolfazl.nmock.dataSource.mock.ImportedMockDataSourceImpl
import me.abolfazl.nmock.dataSource.mock.NormalMockDataSource
import me.abolfazl.nmock.dataSource.mock.NormalMockDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindNormalMockDataSource(
        normalMockDataSourceImpl: NormalMockDataSourceImpl
    ): NormalMockDataSource

    @Binds
    @Singleton
    abstract fun bindImportedMockDataSource(
        importedMockDataSourceImpl: ImportedMockDataSourceImpl
    ): ImportedMockDataSource
}