package me.abolfazl.nmock.model.apiService.models.locationInfo

import com.google.gson.annotations.SerializedName

data class LocationInfoModel(
    @SerializedName("city")
    val city: String,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    @SerializedName("municipality_zone")
    val municipalityZone: String,
    @SerializedName("neighbourhood")
    val neighbourhood: String,
    @SerializedName("route_name")
    val routeName: String,
    @SerializedName("route_type")
    val routeType: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("in_traffic_zone")
    val inTrafficZone: Boolean,
    @SerializedName("in_odd_even_zone")
    val inOddEvenZone: Boolean,
    @SerializedName("place")
    val place: String?,
    @SerializedName("district")
    val district: String,
    @SerializedName("village")
    val village: String?
)