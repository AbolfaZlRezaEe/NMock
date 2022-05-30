package me.abolfazl.nmock.view.editor

import me.abolfazl.nmock.repository.models.MockDataClass
import org.neshan.common.model.LatLng

// todo: we should check this model and optimize that...
data class MockEditorState(
    val originAddress: String? = null,
    val destinationAddress: String? = null,
    val originLocation: LatLng? = null,
    val destinationLocation: LatLng? = null,
    val lineVector: ArrayList<List<LatLng>>? = null,
    val speed: Int? = null,
    val mockId: Long? = null,
    val mockInformation: MockDataClass? = null
)