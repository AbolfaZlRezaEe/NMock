package me.abolfazl.nmock.model.apiService.models.auth

import com.google.gson.annotations.SerializedName

data class SignUpModel(
    @SerializedName("message")
    val message: String
)