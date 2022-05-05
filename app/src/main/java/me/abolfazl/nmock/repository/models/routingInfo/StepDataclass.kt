package me.abolfazl.nmock.repository.models.routingInfo

import org.neshan.common.model.LatLng
import org.neshan.common.utils.PolylineEncoding

data class StepDataclass(
    val polyline: String
) {
    fun getLine(): List<LatLng> {
        return PolylineEncoding.decode(polyline)
    }
}