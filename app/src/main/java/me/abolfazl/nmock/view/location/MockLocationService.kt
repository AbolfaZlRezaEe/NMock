package me.abolfazl.nmock.view.location

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
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
        val notification = me.abolfazl.nmock.utils.managers.NotificationManager
            .createForegroundNotification(
                context = this,
                channelId = resources.getString(R.string.applicationNotificationChannelId),
                channelDescription = resources.getString(R.string.applicationNotificationChannelDescription),
                smallIcon = R.drawable.ic_location_service,
                title = resources.getString(R.string.weHaveYourLocation),
                description = resources.getString(R.string.locationServiceDescription),
                onGoing = true,
                autoCancel = false
            )

        startForeground(Constant.APPLICATION_NOTIFICATION_ID, notification)
        logger.writeLog(value = "location service started!")
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

    private fun detachLocationCallback() {
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
        logger.writeLog(value = "stop providing location from service")
        detachLocationCallback()
    }

    fun startProvidingLocation() {
        logger.writeLog(value = "start providing location from service")
        attachLocationCallback()
    }

    fun stopLocationService() {
        logger.writeLog(value = "location service will be down soon...")
        stopForeground(true)
        stopSelf()
    }

    inner class LocationBinder : Binder() {
        fun getService(): MockLocationService = this@MockLocationService
    }

    override fun onDestroy() {
        detachLocationCallback()
        super.onDestroy()
    }
}