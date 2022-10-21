package me.abolfazl.nmock.repository.routingInfo.models

import org.neshan.common.utils.PolylineEncoding

data class StepDataclass(
    val polyline: String
) {
    fun getLine(): List<com.google.android.gms.maps.model.LatLng> {
        val decodedPolyline = PolylineEncoding.decode(polyline)
        val result: MutableList<com.google.android.gms.maps.model.LatLng> = mutableListOf()
        decodedPolyline.forEach { neshanLatLng ->
            result.add(
                com.google.android.gms.maps.model.LatLng(
                    neshanLatLng.latitude, neshanLatLng.longitude
                )
            )
        }
        return result
    }
}