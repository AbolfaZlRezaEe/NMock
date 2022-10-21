package me.abolfazl.nmock.utils.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

object MarkerManager {

    // Marker Names
    const val MARKER_DRAWABLE_NAME_ORIGIN = "ic_origin_marker"
    const val MARKER_DRAWABLE_NAME_DESTINATION = "ic_destination_marker"
    const val MARKER_DRAWABLE_NAME_CURRENT_USER_LOCATION = "current_location_marker"
    const val MARKER_DRAWABLE_NAME_CURRENT_MOCK_LOCATION = "current_mock_location"

    // Marker Size
    private const val MARKER_WIDTH_SIZE = 55
    private const val MARKER_HEIGHT_SIZE = 55

    fun createMarkerOption(
        context: Context,
        drawableName: String,
        position: LatLng,
        alpha: Float = 1f,
    ): MarkerOptions {
        val iconBitmap = resizeMarkerIcon(
            context = context,
            width = MARKER_WIDTH_SIZE,
            height = MARKER_HEIGHT_SIZE,
            drawableName = drawableName
        )
        return MarkerOptions().apply {
            icon(BitmapDescriptorFactory.fromBitmap(iconBitmap))
            position(position)
            alpha(alpha)
        }
    }

    private fun resizeMarkerIcon(
        context: Context,
        width: Int,
        height: Int,
        drawableName: String
    ): Bitmap {
        val iconBitmap = BitmapFactory.decodeResource(
            context.resources,
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        )
        return Bitmap.createScaledBitmap(iconBitmap, width, height, false)
    }
}