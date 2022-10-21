package me.abolfazl.nmock.utils.managers

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions

object MapManager {

    fun getMapOptions(): GoogleMapOptions {
        return GoogleMapOptions().apply {
            mapType(GoogleMap.MAP_TYPE_NORMAL)
            compassEnabled(true)
            rotateGesturesEnabled(true)
            tiltGesturesEnabled(true)
            scrollGesturesEnabled(true)
        }
    }

    fun setTrafficLayerVisibility(map: GoogleMap) {
        map.isTrafficEnabled = false
    }
}