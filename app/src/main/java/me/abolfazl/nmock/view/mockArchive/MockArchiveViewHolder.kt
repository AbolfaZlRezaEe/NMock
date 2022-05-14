package me.abolfazl.nmock.view.mockArchive

import androidx.recyclerview.widget.RecyclerView
import me.abolfazl.nmock.databinding.ItemMockBinding
import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.utils.Constant

class MockArchiveViewHolder constructor(
    private val binding: ItemMockBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        title: String,
        description: String,
        @MockProvider
        mockProvider: String,
        speed: Int
    ) {
        binding.titleTextView.text = title
        binding.descriptionTextView.text = description
        val provider = if (mockProvider == Constant.PROVIDER_GPS) "GPS" else "NETWORK"
        binding.providerTextView.text = "Provider: $provider"
        binding.speedTextView.text = "Speed: $speed KM/H"
    }
}