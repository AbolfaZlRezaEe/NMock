package me.abolfazl.nmock.view.mockEditor

import org.neshan.common.model.LatLng

data class MockEditorState(
    val originAddress: String? = null,
    val destinationAddress: String? = null,
    val lineVector: ArrayList<List<LatLng>>? = null
)