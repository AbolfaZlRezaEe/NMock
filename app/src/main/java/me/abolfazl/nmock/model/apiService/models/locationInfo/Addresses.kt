package me.abolfazl.nmock.model.apiService.models.locationInfo

import com.google.gson.annotations.SerializedName

data class Addresses(
    @SerializedName("components")
    val components: List<Component>,
    @SerializedName("formatted")
    val formatted: String
)