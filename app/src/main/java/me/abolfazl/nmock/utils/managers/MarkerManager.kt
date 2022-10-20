package me.abolfazl.nmock.utils.managers

import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.carto.graphics.Bitmap
import com.carto.styles.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.internal.utils.BitmapUtils
import org.neshan.mapsdk.model.Marker

object MarkerManager {

    private const val ID_ELEMENT_META_DATA = "id"
    const val ELEMENT_ID_ORIGIN_MARKER = "ORIGIN_MARKER"
    const val ELEMENT_ID_DESTINATION_MARKER = "DESTINATION_MARKER"
    const val ELEMENT_ID_CURRENT_LOCATION_MARKER = "CURRENT_LOCATION_MARKER"

    private const val NORMAL_MARKER_SIZE = 32F

    @NonNull
    fun createMarkerOption(
        @DrawableRes icon: Int,
        position: com.google.android.gms.maps.model.LatLng,
        alpha: Float = 1f,
    ): MarkerOptions {
        return MarkerOptions().apply {
            icon(BitmapDescriptorFactory.fromResource(icon))
            position(position)
            alpha(alpha)
        }
    }

    fun createMarker(
        @NonNull location: LatLng,
        @DrawableRes drawableRes: Int,
        @Nullable context: Context?,
        @Nullable elementId: String? = null,
        markerSize: Float = NORMAL_MARKER_SIZE
    ): Marker? {
        val markerBitmap = getBitmapFromResourceId(drawableRes, context) ?: return null
        val markerStyle = createMarkerStyle(bitmap = markerBitmap, markerSize = markerSize)
        return Marker(location, markerStyle).apply {
            elementId?.let {
                putMetadata(ID_ELEMENT_META_DATA, it)
            }
        }
    }

    private fun createMarkerStyle(
        @NonNull bitmap: Bitmap,
        markerSize: Float = 32F,
        @Nullable animationStyle: AnimationStyle? = null
    ): MarkerStyle {
        val markerStyleBuilder = MarkerStyleBuilder()
        markerStyleBuilder.bitmap = bitmap
        markerStyleBuilder.size = markerSize
        if (animationStyle == null) {
            val animationStyleBuilder = AnimationStyleBuilder().apply {
                sizeAnimationType = AnimationType.ANIMATION_TYPE_SPRING
            }.buildStyle()
            markerStyleBuilder.animationStyle = animationStyleBuilder
        } else {
            markerStyleBuilder.animationStyle = animationStyle
        }
        return markerStyleBuilder.buildStyle()
    }

    private fun getBitmapFromResourceId(
        @DrawableRes resource: Int,
        @Nullable context: Context?
    ): Bitmap? {
        if (context == null) return null
        return BitmapUtils.createBitmapFromAndroidBitmap(
            BitmapFactory.decodeResource(context.resources, resource)
        )
    }

    fun getMarkerFromLayer(
        layer: ArrayList<Marker>,
        id: String
    ): Marker? {
        layer.forEach { marker ->
            val hasId = marker.hasMetadata(MarkerManager.ID_ELEMENT_META_DATA)
            if (hasId && marker.getMetadata(MarkerManager.ID_ELEMENT_META_DATA) == id)
                return marker
        }
        return null
    }
}