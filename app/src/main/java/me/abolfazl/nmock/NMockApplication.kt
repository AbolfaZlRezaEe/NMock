package me.abolfazl.nmock

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.logger.NMockLogger
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class NMockApplication : Application() {

    @Inject
    lateinit var logger: NMockLogger

    override fun onCreate() {
        super.onCreate()

        logger.attachLogger(javaClass.simpleName)

        Timber.plant(Timber.DebugTree())

        SentryAndroid.init(this) { option ->
            option.dsn = Constant.SENTRY_DSN
            option.setDebug(BuildConfig.DEBUG)
            option.environment = if (BuildConfig.DEBUG) Constant.ENVIRONMENT_DEBUG
            else Constant.ENVIRONMENT_RELEASE
            option.tracesSampleRate = 1.0
        }
    }

    override fun onLowMemory() {
        logger.writeLog(value = "Memory is low!!")
        super.onLowMemory()
    }
}