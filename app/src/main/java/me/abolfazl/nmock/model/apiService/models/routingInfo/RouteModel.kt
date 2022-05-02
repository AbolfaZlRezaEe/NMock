package me.abolfazl.nmock.model.apiService.models.routingInfo

import com.google.gson.annotations.SerializedName

data class RouteModel(
    @SerializedName("legs")
    val legModels: List<LegModel>,
    @SerializedName("overview_polyline")
    val overview_polylineModel: OverviewPolylineModel
)