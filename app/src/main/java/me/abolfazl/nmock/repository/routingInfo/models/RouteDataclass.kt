package me.abolfazl.nmock.repository.routingInfo.models

import com.google.android.gms.maps.model.LatLng

data class RouteDataclass(
    val legModels: List<LegDataclass>,
) {
    fun getRouteLineVector(): ArrayList<List<LatLng>> {
        val result = ArrayList<List<LatLng>>()
        legModels.forEach { leg ->
            leg.stepModels.forEach { step ->
                result.add(step.getLine())
            }
        }
        return result
    }
}
