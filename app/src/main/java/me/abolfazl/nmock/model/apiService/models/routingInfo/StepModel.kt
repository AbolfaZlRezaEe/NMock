package me.abolfazl.nmock.model.apiService.models.routingInfo

import com.google.gson.annotations.SerializedName

data class StepModel(
    @SerializedName("bearing_after")
    val bearing_after: Int,
    @SerializedName("distance")
    val distance: DistanceX,
    @SerializedName("duration")
    val duration: DurationX,
    @SerializedName("exit")
    val exit: Int,
    @SerializedName("instruction")
    val instruction: String,
    @SerializedName("modifier")
    val modifier: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("polyline")
    val polyline: String,
    @SerializedName("rotaryName")
    val rotaryName: String,
    @SerializedName("start_location")
    val start_location: List<Double>,
    @SerializedName("type")
    val type: String
)