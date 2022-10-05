package me.abolfazl.nmock.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.receiver.GPSBroadcastReceiver
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReceiverModule {

    const val GPS_LISTENER_RECEIVER = "GPS_RECEIVER"

    @Singleton
    @Provides
    @Named(GPS_LISTENER_RECEIVER)
    fun provideGPSListenerReceiver() = GPSBroadcastReceiver()
}