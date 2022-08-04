package me.abolfazl.nmock.model.apiService.models.auth

import com.google.gson.annotations.SerializedName

data class SignInModel(
    @SerializedName("message")
    val message: String,
    @SerializedName("token")
    val token: String
)