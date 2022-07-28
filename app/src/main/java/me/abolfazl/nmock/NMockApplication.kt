package me.abolfazl.nmock

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.pusher.pushnotifications.BeamsCallback
import com.pusher.pushnotifications.PushNotifications
import com.pusher.pushnotifications.PusherCallbackError
import com.pusher.pushnotifications.auth.AuthData
import com.pusher.pushnotifications.auth.AuthDataGetter
import com.pusher.pushnotifications.auth.BeamsTokenProvider
import dagger.hilt.android.HiltAndroidApp
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User
import me.abolfazl.nmock.repository.auth.AuthRepository
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.SHARED_FIREBASE_TOKEN
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.managers.SharedManager
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class NMockApplication : Application() {

    @Inject
    lateinit var logger: NMockLogger

    @Inject
    lateinit var androidId: String

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        logger.attachLogger(javaClass.simpleName)

        initializeSentry()
    }

    private fun initializeSentry() {
        SentryAndroid.init(this) { option ->
            option.dsn = Constant.SENTRY_DSN
            option.setDebug(BuildConfig.DEBUG)
            option.environment = if (BuildConfig.DEBUG) Constant.ENVIRONMENT_DEBUG
            else Constant.ENVIRONMENT_RELEASE
            option.tracesSampleRate = 1.0
        }

        Sentry.configureScope { scope ->
            scope.user = provideUserInformation()
        }
    }

    private fun provideUserInformation(): User {
        return User().apply {
            username = androidId
        }
    }

    override fun onLowMemory() {
        logger.writeLog(value = "Memory is low!!")
        super.onLowMemory()
    }
}