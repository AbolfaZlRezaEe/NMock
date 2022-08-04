package me.abolfazl.nmock.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepository
import me.abolfazl.nmock.repository.locationInfo.LocationInfoRepositoryImpl
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
}