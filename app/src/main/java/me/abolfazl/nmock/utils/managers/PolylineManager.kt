package me.abolfazl.nmock.utils.managers

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import me.abolfazl.nmock.R

object PolylineManager {

    const val POLYLINE_NORMAL_WIDTH_SIZE = 12F

    fun createPolylineOption(
        vector: ArrayList<List<LatLng>>,
        width: Float = POLYLINE_NORMAL_WIDTH_SIZE,
        @ColorRes colorResource: Int = R.color.colorPrimaryDark,
        context: Context,
        jointType: Int = JointType.ROUND
    ): PolylineOptions {
        return PolylineOptions().apply {
            width(width)
            color(ContextCompat.getColor(context, colorResource))
            startCap(RoundCap())
            endCap(RoundCap())
            jointType(jointType)
            vector.forEach { line ->
                addAll(line)
            }
        }
    }
}
