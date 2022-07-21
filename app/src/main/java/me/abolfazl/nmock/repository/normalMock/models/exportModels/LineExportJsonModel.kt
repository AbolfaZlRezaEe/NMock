package me.abolfazl.nmock.repository.normalMock.models.exportModels

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LineExportJsonModel(
    @Json(name = "id")
    val id: Long,
    @Json(name = "latitude")
    val latitude: Double,
    @Json(name = "longitude")
    val longitude: Double,
    @Json(name = "time")
    val time: Long,
    @Json(name = "elapsed_real_time")
    val elapsedRealTime: Long
)
