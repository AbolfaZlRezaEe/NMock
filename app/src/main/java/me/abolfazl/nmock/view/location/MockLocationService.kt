package me.abolfazl.nmock.view.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import me.abolfazl.nmock.R
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import javax.inject.Inject

@AndroidEntryPoint
class MockLocationService : Service() {

    private val binder = LocationBinder()
    private var callback: ((Location?, OneTimeEmitter?) -> Unit)? = null

    // Location stuff
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var lastLocation: Location? = null

    @Inject
    lateinit var logger: NMockLogger

    override fun onCreate() {
        super.onCreate()
        logger.disableLogHeaderForThisClass()
        logger.setClassInformationForEveryLog(javaClass.simpleName)
        startForegroundService()

        initializeLocationStuff()
        attachLocationCallback()
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.current_location_marker)
            .setContentTitle(resources.getString(R.string.weHaveYourLocation))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(resources.getString(R.string.locationServiceDescription))
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Constant.NOTIFICATION_CHANNEL_ID,
                Constant.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_NONE
            )
            notificationChannel.description =
                resources.getString(R.string.notificationChannelDescription)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        startForeground(Constant.NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    private fun initializeLocationStuff() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create()?.apply {
            interval = Constant.LOCATION_INTERVAL
            fastestInterval = Constant.LOCATION_FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) return

                var goodLocation = locationResult.locations[0]
                locationResult.locations.forEach { location ->
                    if (location.accuracy > goodLocation.accuracy) {
                        goodLocation = location
                    }
                }
                lastLocation = goodLocation
                callback?.invoke(goodLocation, null)
            }
        }
    }

    private fun detachLocationCallback(){
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }

    private fun attachLocationCallback() {
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun setServiceCallback(callback: (Location?, OneTimeEmitter?) -> Unit) {
        this.callback = callback
    }

    fun getLastLocation(): Location? {
        return lastLocation
    }

    fun stopProvidingLocation() {
        detachLocationCallback()
    }

    fun startProvidingLocation() {
        attachLocationCallback()
    }

    inner class LocationBinder : Binder() {
        fun getService(): MockLocationService = this@MockLocationService
    }

    override fun onDestroy() {
        detachLocationCallback()
        super.onDestroy()
    }
}