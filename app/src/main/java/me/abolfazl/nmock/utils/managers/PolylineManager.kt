package me.abolfazl.nmock.utils.managers

import androidx.annotation.ColorRes
import androidx.annotation.NonNull
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import me.abolfazl.nmock.R

object PolylineManager {

    fun createPolylineOption(
        positionList: List<LatLng>,
    ): PolylineOptions {
        return PolylineOptions().apply {
            positionList.forEach { latLng ->
                add(latLng)
            }
        }
    }

    fun setPolylineStyle(
        @NonNull polylineOptions: PolylineOptions,
        width: Float = 8f,
        @ColorRes color: Int = R.color.colorPrimaryDark,
        jointType: Int = JointType.ROUND
    ): PolylineOptions {
        return polylineOptions.apply {
            width(width)
            color(color)
            jointType(jointType)
        }
    }
}
