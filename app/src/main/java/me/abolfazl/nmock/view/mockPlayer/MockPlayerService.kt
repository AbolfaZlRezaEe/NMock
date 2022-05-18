package me.abolfazl.nmock.view.mockPlayer

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.abolfazl.nmock.R
import me.abolfazl.nmock.utils.Constant
import org.neshan.common.model.LatLng
import timber.log.Timber
import kotlin.math.*

class MockPlayerService : Service(), LocationListener {

    private val nMockBinder = MockPlayerBinder()
    private var lineVector: List<LatLng>? = null
    private var lengthIndexedLine: LengthIndexedLine? = null

    private var locationManager: LocationManager? = null
    private var mockStillRunning = false

    private var speed = 0
    private var index = 0.0
    private var ratio: Double = 0.00006

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
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.nmock_logo_notifcation)
            .setContentIntent(getNMockIntent())
            .setContentTitle(getString(R.string.notificationTitle))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.notificationDescription))
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
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
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

    fun startCreatingMockLocations(): Flow<Location> = flow {
        while (mockStillRunning) {
            if (lengthIndexedLine == null) {
                processLineVector()
            }
            if (speed == 0) {
                // todo: speed error
                mockStillRunning = false
                return@flow
            }
            lengthIndexedLine?.let { line ->
                val firstCoordinate: Coordinate?
                val secondCoordinate: Coordinate?
                if (line.isValidIndex(index)) {
                    firstCoordinate = line.extractPoint(index)
                    secondCoordinate = line.extractPoint(index + ratio)
                    index += ratio
                } else {
                    // trip was finished and we should send a event!
                    mockStillRunning = false
                    return@flow
                }
                if (firstCoordinate == null || secondCoordinate == null) {
                    // todo: coordination Error
                    mockStillRunning = false
                    return@flow
                }
                val distance: Double = distanceBetweenTwoCoordinates(
                    first = LatLng(firstCoordinate.x, firstCoordinate.y),
                    second = LatLng(secondCoordinate.x, secondCoordinate.y)
                )
                val delay: Double = (distance / (speed / 3.6)) * 1000
                val location = Location(Constant.TYPE_GPS)
                location.speed = speed.toFloat()
                location.bearing = 0F
                location.latitude = firstCoordinate.x
                location.longitude = firstCoordinate.y
                location.accuracy = ratio.toFloat()
                location.time = System.currentTimeMillis()
                location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                locationManager?.setTestProviderLocation(Constant.TYPE_GPS, location)
                emit(location)
                delay(delay.toLong())
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

    fun mockIsRunning(): Boolean {
        return mockStillRunning
    }

    fun pauseOrPlayMock() {
        mockStillRunning = !mockStillRunning
    }

    fun removeMockProvider() {
        mockStillRunning = false
        locationManager?.setTestProviderEnabled(Constant.TYPE_GPS, false)
        locationManager?.removeTestProvider(Constant.TYPE_GPS)
    }

    fun resetResources() {
        lineVector = null
        speed = 0
        mockStillRunning = false
        ratio = 0.00006
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
        removeMockProvider()
    }

    override fun onLocationChanged(p0: Location) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}