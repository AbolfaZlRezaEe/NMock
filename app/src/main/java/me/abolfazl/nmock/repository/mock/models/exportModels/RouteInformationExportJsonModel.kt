package me.abolfazl.nmock.repository.mock.models.exportModels

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RouteInformationExportJsonModel(
    @Json(name = "origin_location")
    val originLocation: String,
    @Json(name = "destination_location")
    val destinationLocation: String,
    @Json(name = "route_lines")
    val routeLines: List<LineExportJsonModel>
)
