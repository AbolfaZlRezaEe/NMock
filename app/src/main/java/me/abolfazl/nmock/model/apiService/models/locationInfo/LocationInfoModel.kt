package me.abolfazl.nmock.model.apiService.models.locationInfo

import com.google.gson.annotations.SerializedName

data class LocationInfoModel(
    @SerializedName("addresses")
    val addresses: List<Addresses>,
    @SerializedName("city")
    val city: String,
    @SerializedName("formatted_address")
    val formatted_address: String,
    @SerializedName("municipality_zone")
    val municipality_zone: String,
    @SerializedName("neighbourhood")
    val neighbourhood: String,
    @SerializedName("route_name")
    val route_name: String,
    @SerializedName("route_type")
    val route_type: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("status")
    val status: String
)