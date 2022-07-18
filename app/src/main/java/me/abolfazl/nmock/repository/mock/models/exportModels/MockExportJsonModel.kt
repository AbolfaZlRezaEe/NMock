package me.abolfazl.nmock.repository.mock.models.exportModels

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MockExportJsonModel(
    @Json(name = "file_created_at")
    val fileCreatedAt: String,
    @Json(name= "file_owner")
    val fileOwner: String,
    @Json(name = "version_code")
    val versionCode: Int,
    @Json(name = "mock_information")
    val mockInformation: MockInformationExportJsonModel,
    @Json(name= "route_information")
    val routeInformation: RouteInformationExportJsonModel
)