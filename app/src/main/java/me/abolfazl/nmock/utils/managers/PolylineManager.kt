package me.abolfazl.nmock.utils.managers

import android.graphics.Paint.Join
import androidx.annotation.ColorRes
import androidx.annotation.NonNull
import com.carto.graphics.Color
import com.carto.styles.LineStyle
import com.carto.styles.LineStyleBuilder
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.PolylineOptions
import me.abolfazl.nmock.R
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.MapView
import org.neshan.mapsdk.model.Polyline

object PolylineManager {

    fun createPolylineOption(
        positionList: List<com.google.android.gms.maps.model.LatLng>,
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

    fun createLineStyle(
        width: Float = 8F,
        color: Color = Color(0XffA86E00.toInt())
    ): LineStyle {
        return LineStyleBuilder().apply {
            this.width = width
            this.color = color
        }.buildStyle()
    }

    fun createLineFromVectors(
        lineStyle: LineStyle,
        lineVector: List<LatLng>
    ): Polyline {
        val lines = ArrayList<LatLng>(lineVector)
        return Polyline(lines, lineStyle)
    }


    fun drawLineOnMap(
        mapView: MapView,
        polylineLayer: ArrayList<Polyline>? = null,
        vector: ArrayList<List<LatLng>>
    ) {
        val lineStyle = createLineStyle()
        vector.forEach { lineVector ->
            val polyLine = createLineFromVectors(lineStyle, lineVector)
            mapView.addPolyline(polyLine)
            polylineLayer?.add(polyLine)
        }
    }
}
