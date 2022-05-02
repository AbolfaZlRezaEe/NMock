package me.abolfazl.nmock.model.apiService.models.routingInfo

import com.google.gson.annotations.SerializedName

data class DistanceModel(
    @SerializedName("text")
    val text: String,
    @SerializedName("value")
    val value: Double
)