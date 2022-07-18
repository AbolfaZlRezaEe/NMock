package me.abolfazl.nmock.view.archive

import androidx.recyclerview.widget.RecyclerView
import com.carto.core.ScreenPos
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ItemMockArchiveBinding
import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.managers.CameraManager
import me.abolfazl.nmock.utils.managers.LineManager
import me.abolfazl.nmock.utils.managers.MarkerManager
import me.abolfazl.nmock.utils.toPixel
import org.neshan.common.model.LatLng

class MockArchiveViewHolder constructor(
    private val binding: ItemMockArchiveBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        title: String,
        @MockProvider mockProvider: String,
        originLocation: LatLng,
        destinationLocation: LatLng,
        lineVector: ArrayList<List<LatLng>>?,
        speed: Int,
    ) {
        // Set mock information on card:
        binding.mockTitleTextView.text = title
        val provider =
            if (mockProvider == Constant.PROVIDER_GPS)
                itemView.resources.getString(R.string.gps)
            else
                itemView.resources.getString(R.string.network)
        binding.providerTextView.text =
            "${itemView.resources.getString(R.string.provider)}: $provider"
        binding.speedTextView.text =
            "${itemView.resources.getString(R.string.speed)}:" +
                    " $speed ${itemView.resources.getString(R.string.km_h)}"

        // Draw mock information on map:
        MarkerManager.createMarker(
            location = originLocation,
            drawableRes = R.drawable.ic_origin_marker,
            context = itemView.context,
            elementId = MarkerManager.ELEMENT_ID_ORIGIN_MARKER,
        )

        MarkerManager.createMarker(
            location = destinationLocation,
            drawableRes = R.drawable.ic_destination_marker,
            context = itemView.context,
            elementId = MarkerManager.ELEMENT_ID_DESTINATION_MARKER,
        )

        lineVector?.let{
            LineManager.drawLineOnMap(
                mapView = binding.mapView,
                vector = lineVector
            )
        }

        CameraManager.moveCameraToTripLine(
            mapView = binding.mapView,
            screenPos = ScreenPos(
                binding.mapView.x + 32.toPixel(itemView.context),
                binding.mapView.y + 32.toPixel(itemView.context)
            ),
            origin = originLocation,
            destination = destinationLocation
        )
    }
}