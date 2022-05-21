package me.abolfazl.nmock.view.player

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LineString
import com.vividsolutions.jts.linearref.LengthIndexedLine
import me.abolfazl.nmock.R
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_COORDINATORS_ERROR
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_SPEED_ERROR
import org.neshan.common.model.LatLng
import timber.log.Timber
import kotlin.math.*

class MockPlayerService : Service(), LocationListener {

    companion object {
        var SERVICE_IS_RUNNING = false
        const val KILL_SERVICE = "KILL_SERVICE!"
    }

    private val nMockBinder = MockPlayerBinder()
    private var lineVector: List<LatLng>? = null
    private var lengthIndexedLine: LengthIndexedLine? = null

    private var locationManager: LocationManager? = null
    private var mockStillRunning = false
    private var locationListener: ((Location?, OneTimeEmitter<String>?) -> Unit)? = null

    private var speed = 0
    private var index = 0.0
    private var ratio: Double = Constant.DEFAULT_RATIO

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    @SuppressLint("MissingPermission")
    fun initializeMockProvider() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager?.let { manager ->
            try {
                manager.addTestProvider(
                    Constant.TYPE_GPS,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    3, // powerRequirement should be in range [1,3] for android 11
                    1 // accuracy should be in range [1,2] for android 11
                )
                manager.setTestProviderEnabled(Constant.TYPE_GPS, true)
                manager.requestLocationUpdates(Constant.TYPE_GPS, 0L, 0F, this)
            } catch (exception: IllegalArgumentException) {
                Timber.e(exception.message)
            } catch (exception: SecurityException) {
                Timber.e(exception.message)
            }
        }
        SERVICE_IS_RUNNING = true
        startCreatingMockLocations()
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.nmock_logo_notifcation)
            .setContentIntent(getNMockIntent())
            .setContentTitle(resources.getString(R.string.notificationTitle))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(resources.getString(R.string.notificationDescription))
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
            notificationChannel.description = Constant.NOTIFICATION_CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(notificationChannel)
        }

        startForeground(Constant.NOTIFICATION_ID, notification)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getNMockIntent(): PendingIntent {
        return Intent(this, MockPlayerActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handlingIntent(intent)
        return START_NOT_STICKY
    }

    private fun handlingIntent(intent: Intent?) {
        if (intent == null) return
        val mustBeKilled = intent.getBooleanExtra(KILL_SERVICE, false)
        if (!mustBeKilled) return
        if (!mockStillRunning) return
        removeMockProvider()
        resetResources()
        stopIdleService()
    }

    override fun onBind(p0: Intent?): IBinder {
        return nMockBinder
    }

    private fun processLineVector() {
        lineVector?.let {
            lengthIndexedLine = LengthIndexedLine(convertLineVectorToLineString(it))
        }
    }

    private fun convertLineVectorToLineString(locations: List<LatLng>): LineString {
        val lineCoordinates = Array(locations.size) { Coordinate() }
        locations.forEachIndexed { index, latLng ->
            lineCoordinates[index] = Coordinate(latLng.latitude, latLng.longitude)
        }
        return GeometryFactory().createLineString(lineCoordinates)
    }

    private fun distanceBetweenTwoCoordinates(
        first: LatLng,
        second: LatLng
    ): Double {
        val lat1: Double = first.latitude
        val lat2: Double = second.latitude
        val long1: Double = first.longitude
        val long2: Double = second.longitude

        val r = 6371 // Radius of the earth

        val latitudeDistance: Double = Math.toRadians(lat2 - lat1)
        val longitudeDistance: Double = Math.toRadians(long2 - long1)

        val a: Double = sin(latitudeDistance / 2) *
                sin(latitudeDistance / 2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(longitudeDistance / 2) *
                sin(longitudeDistance / 2)

        val c: Double = 2 * atan2(sqrt(a), sqrt(1 - a))
        var distance: Double = r * c * 1000 // Convert to meters

        distance = distance.pow(2.0)
        return sqrt(distance)
    }

    private fun startCreatingMockLocations() {
        if (mockStillRunning) {
            if (lengthIndexedLine == null) {
                processLineVector()
            }
            if (speed == 0) {
                locationListener?.invoke(
                    null, OneTimeEmitter(
                        exception = EXCEPTION_SPEED_ERROR,
                        message = null
                    )
                )
                mockStillRunning = false
                return
            }
            lengthIndexedLine?.let { line ->
                val firstCoordinate: Coordinate?
                val secondCoordinate: Coordinate?
                if (line.isValidIndex(index)) {
                    firstCoordinate = line.extractPoint(index)
                    secondCoordinate = line.extractPoint(index + ratio)
                    index += ratio
                } else {
                    locationListener?.invoke(null, OneTimeEmitter(message = "Trip was finished!"))
                    resetResources()
                    return
                }
                if (firstCoordinate == null || secondCoordinate == null) {
                    locationListener?.invoke(
                        null,
                        OneTimeEmitter(exception = EXCEPTION_COORDINATORS_ERROR, message = null)
                    )
                    mockStillRunning = false
                    return
                }
                val distance: Double = distanceBetweenTwoCoordinates(
                    first = LatLng(firstCoordinate.x, firstCoordinate.y),
                    second = LatLng(secondCoordinate.x, secondCoordinate.y)
                )
                val delay: Double = (distance / (speed / 3.6)) * 1000
                val location = Location(Constant.TYPE_GPS)
                location.speed = (speed / 3.6).toFloat()
                location.bearing = 0F
                location.latitude = firstCoordinate.x
                location.longitude = firstCoordinate.y
                location.accuracy = ratio.toFloat()
                location.time = System.currentTimeMillis()
                location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                locationManager?.setTestProviderLocation(Constant.TYPE_GPS, location)
                locationListener?.invoke(location, OneTimeEmitter(null, null))
                Handler(Looper.getMainLooper()).postDelayed({
                    startCreatingMockLocations()
                }, delay.toLong())
            }
        }
    }

    inner class MockPlayerBinder : Binder() {
        fun getService(): MockPlayerService = this@MockPlayerService
    }

    fun setLineVectorForProcessing(lineVector: List<LatLng>) {
        this.lineVector = lineVector
    }

    fun setMockSpeed(speed: Int) {
        this.speed = speed
    }

    fun setLocationChangedListener(
        callback: (Location?, OneTimeEmitter<String>?) -> Unit
    ) {
        this.locationListener = callback
    }

    fun mockIsRunning(): Boolean {
        return mockStillRunning
    }

    fun pauseOrPlayMock() {
        mockStillRunning = !mockStillRunning
    }

    fun removeMockProvider() {
        mockStillRunning = false
        try {
            locationManager?.setTestProviderEnabled(Constant.TYPE_GPS, false)
            locationManager?.removeTestProvider(Constant.TYPE_GPS)
        } catch (exception: java.lang.IllegalArgumentException) {
            Timber.e(exception.message)
        }
    }

    fun resetResources() {
        SERVICE_IS_RUNNING = false
        lineVector = null
        speed = 0
        mockStillRunning = false
        ratio = Constant.DEFAULT_RATIO
        index = 0.0
    }

    fun shouldReInitialize(): Boolean {
        return lineVector == null || speed == 0
    }

    fun stopIdleService() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        SERVICE_IS_RUNNING = false
        removeMockProvider()
    }

    override fun onLocationChanged(p0: Location) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}