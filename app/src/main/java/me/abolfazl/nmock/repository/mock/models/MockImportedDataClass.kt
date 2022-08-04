package me.abolfazl.nmock.repository.mock.models

import me.abolfazl.nmock.model.database.mocks.MockProvider
import me.abolfazl.nmock.model.database.mocks.MockType
import org.neshan.common.model.LatLng

data class MockImportedDataClass(
    val id: Long? = null,
    val name: String,
    val description: String,
    val originLocation: LatLng,
    val destinationLocation: LatLng,
    val originAddress: String?,
    val destinationAddress: String?,
    @MockType
    val type: String,
    var speed: Int = 0,
    val lineVector: ArrayList<List<LatLng>>? = null,
    val bearing: Float,
    val accuracy: Float,
    @MockProvider
    val provider: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val fileCreatedAt:String,
    val fileOwner:String,
    val versionCode:Int,
)
