package me.abolfazl.nmock.utils.managers

import com.carto.core.ScreenBounds
import com.carto.core.ScreenPos
import org.neshan.common.model.LatLng
import org.neshan.common.model.LatLngBounds
import org.neshan.mapsdk.MapView
import kotlin.math.max
import kotlin.math.min

object CameraManager {

    fun moveCameraToTripLine(
        mapView: MapView,
        screenPos: ScreenPos,
        origin: LatLng,
        destination: LatLng
    ) {
        val minX = min(origin.latitude, destination.latitude)
        val maxX = max(origin.latitude, destination.latitude)
        val minY = min(origin.longitude, destination.longitude)
        val maxY = max(origin.longitude, destination.longitude)

        val latLngBounds = LatLngBounds(LatLng(minX, minY), LatLng(maxX, maxY))
        val screenBounds = ScreenBounds(
            screenPos,
            ScreenPos(mapView.width.toFloat(), mapView.height.toFloat())
        )

        mapView.moveToCameraBounds(latLngBounds, screenBounds, true, 0.5f)
    }

    fun focusOnUserLocation(
        mapView: MapView,
        location: LatLng,
        zoom: Float = 15F
    ) {
        mapView.moveCamera(location, 0.5F)
        mapView.setZoom(zoom, 0.5F)
    }
}