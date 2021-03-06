package me.abolfazl.nmock.model.apiService.models.routingInfo

import com.google.gson.annotations.SerializedName

data class RoutingInfoModel(
    @SerializedName("routes")
    val routeModels: List<RouteModel>
)