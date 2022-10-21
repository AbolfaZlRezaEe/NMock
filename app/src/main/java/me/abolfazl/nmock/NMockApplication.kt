package me.abolfazl.nmock

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import com.pusher.pushnotifications.PushNotifications
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import me.abolfazl.nmock.di.NetworkModule
import me.abolfazl.nmock.di.UtilsModule
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.logger.NMockLogger
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named


@HiltAndroidApp
class NMockApplication : Application() {

    @Inject
    lateinit var logger: NMockLogger

    @Inject
    @Named(UtilsModule.INJECT_STRING_ANDROID_ID)
    lateinit var androidId: String

    @Inject
    @Named(NetworkModule.PUSHER_INSTANCE_ID_NAME)
    lateinit var pusherInstanceId: String

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        logger.attachLogger(javaClass.simpleName)

        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) {
            if (it == MapsInitializer.Renderer.LATEST) {
                logger.writeLog(value = "Google Map initialized with LATEST renderer")
            }
        }

        initializeSentry()

        PushNotifications.start(applicationContext, pusherInstanceId);
    }

    private fun initializeSentry() {
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