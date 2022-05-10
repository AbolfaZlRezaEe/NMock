package me.abolfazl.nmock.repository.models

import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.model.database.MockType
import org.neshan.common.model.LatLng

data class MockDataClass(
    val mockName: String,
    val mockDescription: String,
    @MockType
    val mockType: String,
    val speed: Int,
    val lineVector: ArrayList<List<LatLng>>
)
