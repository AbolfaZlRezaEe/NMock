package me.abolfazl.nmock.model.apiService.models.routingInfo

import com.google.gson.annotations.SerializedName
import org.neshan.common.model.LatLng

data class Route(
    @SerializedName("legs")
    val legs: List<Leg>,
) {
    fun getRouteLineVector(): List<List<LatLng>> {
        val result = ArrayList<List<LatLng>>()

        legs.forEach { leg ->
            leg.steps.forEach { step ->
                result.add(step.getLine())
            }
        }

        return result
    }
}