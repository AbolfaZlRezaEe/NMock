package me.abolfazl.nmock.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.repository.auth.AuthRepository
import me.abolfazl.nmock.repository.auth.AuthRepositoryImpl
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepository
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepositoryImpl
import me.abolfazl.nmock.repository.mock.MockRepository
import me.abolfazl.nmock.repository.mock.MockRepositoryImpl
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
    abstract fun bindMockRepository(
        mockRepositoryImpl: MockRepositoryImpl
    ): MockRepository

    @Singleton
    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}