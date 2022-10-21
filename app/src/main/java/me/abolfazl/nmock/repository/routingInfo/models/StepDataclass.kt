package me.abolfazl.nmock.repository.routingInfo.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil


data class StepDataclass(
    val polyline: String
) {
    fun getLine(): List<LatLng> {
        return PolyUtil.decode(polyline)
    }
}