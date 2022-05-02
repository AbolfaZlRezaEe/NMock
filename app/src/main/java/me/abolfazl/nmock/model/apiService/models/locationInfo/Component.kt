package me.abolfazl.nmock.model.apiService.models.locationInfo

import com.google.gson.annotations.SerializedName

data class Component(
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String
)