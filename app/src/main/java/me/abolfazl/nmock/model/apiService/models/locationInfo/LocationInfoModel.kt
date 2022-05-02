package me.abolfazl.nmock.model.apiService.models.locationInfo

import com.google.gson.annotations.SerializedName

data class LocationInfoModel(
    @SerializedName("city")
    val city: String,
    @SerializedName("formatted_address")
    val fullAddress: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("status")
    val httpStatus: String
)