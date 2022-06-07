package me.abolfazl.nmock.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.logger.NMockLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideNMockLogger(
        @ApplicationContext context: Context
    ): NMockLogger {
        return NMockLogger(fileName = Constant.LOGGER_FILE_NAME, context = context)
    }
}