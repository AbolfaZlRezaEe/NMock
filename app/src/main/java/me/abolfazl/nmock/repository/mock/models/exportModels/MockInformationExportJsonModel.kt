package me.abolfazl.nmock.repository.mock.models.exportModels

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.model.database.MockType

@JsonClass(generateAdapter = true)
data class MockInformationExportJsonModel(
    @MockType
    @Json(name = "type")
    val type: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "origin_address")
    val originAddress: String?,
    @Json(name = "destination_address")
    val destinationAddress: String?,
    @Json(name = "speed")
    val speed: Int,
    @Json(name = "bearing")
    val bearing: Float,
    @Json(name = "accuracy")
    val accuracy: Float,
    @MockProvider
    @Json(name = "provider")
    val provider: String,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "updated_at")
    val updatedAt: String?,
)
