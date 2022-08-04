package me.abolfazl.nmock.view.editor

import me.abolfazl.nmock.model.database.MockDatabaseType
import me.abolfazl.nmock.utils.response.SingleEvent
import org.neshan.common.model.LatLng

data class MockEditorState(
    val id: SingleEvent<Long>? = null,
    val name: SingleEvent<String>? = null,
    val description: SingleEvent<String>? = null,
    val originLocation: SingleEvent<LatLng>? = null,
    val destinationLocation: SingleEvent<LatLng>? = null,
    val originAddress: SingleEvent<String?>? = null,
    val destinationAddress: SingleEvent<String?>? = null,
    val lineVector: SingleEvent<ArrayList<List<LatLng>>>? = null,
    var speed: Int = 0,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    @MockDatabaseType var mockDatabaseType: String? = null
)