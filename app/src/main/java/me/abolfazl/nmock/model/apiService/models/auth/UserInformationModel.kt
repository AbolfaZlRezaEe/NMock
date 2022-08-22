package me.abolfazl.nmock.model.apiService.models.auth

import com.google.gson.annotations.SerializedName

data class UserInformationModel(
    @SerializedName("id")
    val userId: Int,
    @SerializedName("avatar")
    val profilePicture: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("full_name")
    val fullName: String,
)