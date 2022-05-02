package me.abolfazl.nmock.model.apiService.models.routingInfo

import com.google.gson.annotations.SerializedName

data class OverviewPolylineModel(
    @SerializedName("points")
    val points: String
)