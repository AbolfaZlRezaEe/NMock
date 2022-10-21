package me.abolfazl.nmock.repository.routingInfo.models

data class RouteDataclass(
    val legModels: List<LegDataclass>,
) {
    fun getRouteLineVector(): ArrayList<List<com.google.android.gms.maps.model.LatLng>> {
        val result = ArrayList<List<com.google.android.gms.maps.model.LatLng>>()
        legModels.forEach { leg ->
            leg.stepModels.forEach { step ->
                result.add(step.getLine())
            }
        }
        return result
    }
}
