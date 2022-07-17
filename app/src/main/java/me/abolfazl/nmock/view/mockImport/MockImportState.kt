package me.abolfazl.nmock.view.mockImport

import me.abolfazl.nmock.utils.response.SingleEvent
import org.neshan.common.model.LatLng

data class MockImportState(
    val originLocation: SingleEvent<LatLng>? = null,
    val destinationLocation: SingleEvent<LatLng>? = null,
    val originAddress: SingleEvent<String>? = null,
    val destinationAddress: SingleEvent<String>? = null,
    val lineVector:SingleEvent<ArrayList<List<LatLng>>>? = null,
    val name: SingleEvent<String>? = null,
    val description: SingleEvent<String>? = null
)