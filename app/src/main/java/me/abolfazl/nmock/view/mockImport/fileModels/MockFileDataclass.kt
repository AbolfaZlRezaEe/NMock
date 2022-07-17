package me.abolfazl.nmock.view.mockImport.fileModels

import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.model.database.MockType
import me.abolfazl.nmock.utils.Constant

data class MockFileDataclass(
    val id: Long? = null,
    @MockType
    val type: String = Constant.TYPE_CUSTOM_CREATE,
    val name: String,
    val description: String,
    val originLocation: String,
    val destinationLocation: String,
    val originAddress: String?,
    val destinationAddress: String?,
    val speed: Int,
    val bearing: Float,
    val accuracy: Float,
    @MockProvider
    val provider: String,
    val createdAt: String,
    val updatedAt: String
)