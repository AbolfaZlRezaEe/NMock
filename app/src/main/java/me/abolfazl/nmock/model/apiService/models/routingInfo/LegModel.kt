package me.abolfazl.nmock.model.apiService.models.routingInfo

import com.google.gson.annotations.SerializedName

data class LegModel(
    @SerializedName("distance")
    val distanceModel: DistanceModel,
    @SerializedName("duration")
    val durationModel: DurationModel,
    @SerializedName("steps")
    val steps: List<StepModel>,
    @SerializedName("summary")
    val summary: String
)