package me.abolfazl.nmock.utils.managers

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object CameraManager {

    // Zoom
    const val USER_CURRENT_LOCATION_NORMAL_ZOOM = 15F

    // Duration
    const val NORMAL_CAMERA_ANIMATION_DURATION = 700 //ms

    // Padding
    const val NORMAL_PATH_FIT_PADDING = 128
    const val TOMUCH_PATH_FIT_PADDING = NORMAL_PATH_FIT_PADDING * 2

    fun fitCameraToPath(
        originPoint: LatLng,
        destinationPoint: LatLng,
        padding: Int = 0,
        mapView: GoogleMap,
        widthMapView: Int,
        heightMapView: Int,
        duration: Int
    ) {
        val pathBounds: LatLngBounds = LatLngBounds.builder()
            .include(originPoint)
            .include(destinationPoint)
            .build()

        mapView.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                pathBounds,
                widthMapView,
                heightMapView,
                padding
            ),
            duration,
            object : GoogleMap.CancelableCallback {
                override fun onCancel() {
                }

                override fun onFinish() {
                }
            }
        )
    }

    fun focusOnLocation(
        location: LatLng,
        mapView: GoogleMap,
        zoom: Float,
        duration: Int
    ) {
        mapView.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                zoom
            ),
            duration,
            object : GoogleMap.CancelableCallback {
                override fun onCancel() {
                }

                override fun onFinish() {
                }
            }
        )
    }
}