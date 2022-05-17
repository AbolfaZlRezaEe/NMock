package me.abolfazl.nmock.utils.managers

import com.carto.graphics.Color
import com.carto.styles.LineStyle
import com.carto.styles.LineStyleBuilder
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.MapView
import org.neshan.mapsdk.model.Polyline

object LineManager {

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
        polylineLayer: ArrayList<Polyline>,
        vector: ArrayList<List<LatLng>>
    ) {
        val lineStyle = createLineStyle()
        vector.forEach { lineVector ->
            val polyLine = createLineFromVectors(lineStyle, lineVector)
            mapView.addPolyline(polyLine)
            polylineLayer.add(polyLine)
        }
    }
}
