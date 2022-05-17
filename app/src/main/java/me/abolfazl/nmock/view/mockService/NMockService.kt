package me.abolfazl.nmock.view.mockService

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
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
import me.abolfazl.nmock.utils.response.Failure
import me.abolfazl.nmock.utils.response.Response
import me.abolfazl.nmock.utils.response.Success
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_COORDINATORS_ERROR
import me.abolfazl.nmock.utils.response.exceptions.EXCEPTION_SPEED_ERROR
import me.abolfazl.nmock.utils.response.exceptions.NMockException
import me.abolfazl.nmock.view.mockPlayer.MockPlayerActivity
import org.neshan.common.model.LatLng
import timber.log.Timber
import kotlin.math.*

class NMockService : Service() {

    private val nMockBinder = NMockBinder()
    private var lineVector: List<LatLng>? = null
    private var lengthIndexedLine: LengthIndexedLine? = null

    private var speed = 0
    private var index = 0.0
    private var ratio: Double = 0.00006

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.nmock_logo_notifcation)
            .setContentIntent(getNMockIntent())
            .setContentTitle(getString(R.string.notificationTitle))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notificationDescription)))
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
        return nMockBinder;
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

    fun getMockLocation(): Flow<Response<Location, NMockException>> = flow {
        if (lengthIndexedLine == null) {
            processLineVector()
        }
        if (speed == 0) {
            emit(Failure(NMockException(type = EXCEPTION_SPEED_ERROR)))
            stopForeground(true)
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
                stopSelf()
                return@flow
            }
            if (firstCoordinate == null || secondCoordinate == null) {
                emit(Failure(NMockException(type = EXCEPTION_COORDINATORS_ERROR)))
                stopSelf()
                return@flow
            }
            val distance: Double = distanceBetweenTwoCoordinates(
                first = LatLng(firstCoordinate.x, firstCoordinate.y),
                second = LatLng(secondCoordinate.x, secondCoordinate.y)
            )
            val delay: Double = (distance / (speed / 3.6)) * 1000
            val location = Location(Constant.PROVIDER_GPS)
            location.speed = speed.toFloat()
            location.bearing = 0F
            location.latitude = firstCoordinate.x
            location.longitude = firstCoordinate.y
            location.accuracy = ratio.toFloat()
            location.time = System.currentTimeMillis()
            location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            emit(Success(location))
            delay(delay.toLong())
        }
    }

    inner class NMockBinder : Binder() {
        fun getService(): NMockService = this@NMockService

        fun setLineVectorForProcessing(lineVector: List<LatLng>) {
            this@NMockService.lineVector = lineVector
        }

        fun setMockSpeed(speed: Int) {
            this@NMockService.speed = speed
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("abolfazl, service was destroyed")
    }
}