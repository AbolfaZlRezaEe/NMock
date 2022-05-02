package me.abolfazl.nmock.model.apiService.models.routingInfo

import com.google.gson.annotations.SerializedName

data class Leg(
    @SerializedName("steps")
    val steps: List<Step>,
)