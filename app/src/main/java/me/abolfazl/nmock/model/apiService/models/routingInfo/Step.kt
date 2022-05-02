package me.abolfazl.nmock.model.apiService.models.routingInfo

import com.google.gson.annotations.SerializedName
import org.neshan.common.model.LatLng
import org.neshan.common.utils.PolylineEncoding

data class Step(
    @SerializedName("polyline")
    val polyline: String,
) {
    fun getLine(): List<LatLng> {
        return PolylineEncoding.decode(polyline)
    }
}