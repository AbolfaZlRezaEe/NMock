package me.abolfazl.nmock.di

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
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

    @Singleton
    @Provides
    fun provideMockShared(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences(Constant.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideNMockLogger(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences,
        androidId: String
    ): NMockLogger {
        return NMockLogger(
            fileName = Constant.LOGGER_FILE_NAME,
            context = context,
            androidId = androidId,
            sharedPreferences = sharedPreferences
        )
    }

    @Provides
    @Singleton
    fun provideAndroidId(
        @ApplicationContext context: Context
    ): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}