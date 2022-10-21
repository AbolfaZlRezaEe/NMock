package me.abolfazl.nmock.utils.managers

import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

object MarkerManager {

    private const val ID_ELEMENT_META_DATA = "id"
    const val ELEMENT_ID_ORIGIN_MARKER = "ORIGIN_MARKER"
    const val ELEMENT_ID_DESTINATION_MARKER = "DESTINATION_MARKER"
    const val ELEMENT_ID_CURRENT_LOCATION_MARKER = "CURRENT_LOCATION_MARKER"

    private const val NORMAL_MARKER_SIZE = 32F

    @NonNull
    fun createMarkerOption(
        @DrawableRes icon: Int,
        position: LatLng,
        alpha: Float = 1f,
    ): MarkerOptions {
        return MarkerOptions().apply {
            icon(BitmapDescriptorFactory.fromResource(icon))
            position(position)
            alpha(alpha)
        }
    }
}