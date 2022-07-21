package me.abolfazl.nmock.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepository
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepositoryImpl
import me.abolfazl.nmock.repository.mock.importedMock.ImportedMockRepository
import me.abolfazl.nmock.repository.mock.importedMock.ImportedMockRepositoryImpl
import me.abolfazl.nmock.repository.mock.normalMock.NormalMockRepository
import me.abolfazl.nmock.repository.mock.normalMock.NormalMockRepositoryImpl
import me.abolfazl.nmock.repository.routingInfo.RoutingInfoRepository
import me.abolfazl.nmock.repository.routingInfo.RoutingInfoRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindLocationInfoRepository(
        locationInfoRepositoryImpl: LocationInfoRepositoryImpl
    ): LocationInfoRepository

    @Singleton
    @Binds
    abstract fun bindRoutingInfoRepository(
        routingInfoRepositoryImpl: RoutingInfoRepositoryImpl
    ): RoutingInfoRepository

    @Singleton
    @Binds
    abstract fun bindNormalMockRepository(
        mockRepositoryImpl: NormalMockRepositoryImpl
    ): NormalMockRepository

    @Singleton
    @Binds
    abstract fun bindImportedMockRepository(
        importedMockRepositoryImpl: ImportedMockRepositoryImpl
    ): ImportedMockRepository
}