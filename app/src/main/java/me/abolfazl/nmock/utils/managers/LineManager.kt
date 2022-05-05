package me.abolfazl.nmock.utils.managers

import com.carto.graphics.Color
import com.carto.styles.LineStyle
import com.carto.styles.LineStyleBuilder
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.model.Polyline

object LineManager {

    fun createLineStyle(
        width: Float = 8F,
        color: Color = Color(0Xff045F7A.toInt())
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
}
