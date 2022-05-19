package me.abolfazl.nmock

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import me.abolfazl.nmock.utils.Constant
import timber.log.Timber


@HiltAndroidApp
class NMockApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        SentryAndroid.init(this) { option ->
            option.dsn = Constant.SENTRY_DSN
            option.setDebug(BuildConfig.DEBUG)
            option.environment = if (BuildConfig.DEBUG) Constant.ENVIRONMENT_DEBUG
            else Constant.ENVIRONMENT_RELEASE
        }
    }
}