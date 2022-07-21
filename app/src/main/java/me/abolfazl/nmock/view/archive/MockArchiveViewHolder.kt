package me.abolfazl.nmock.view.archive

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ItemMockArchiveBinding
import me.abolfazl.nmock.model.database.mocks.MockProvider
import me.abolfazl.nmock.utils.Constant

class MockArchiveViewHolder constructor(
    private val binding: ItemMockArchiveBinding
) : RecyclerView.ViewHolder(binding.root) {

    private var onShareClickListener: ((id: Int) -> Unit)? = null

    fun bind(
        title: String,
        @MockProvider mockProvider: String,
        description: String,
        speed: Int,
        showShareLoadingProgressbar: Boolean
    ) {
        binding.titleTextView.text = title
        binding.descriptionTextView.text = description
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

        binding.shareMaterialButton.setOnClickListener {
            onShareClickListener?.let {
                it.invoke(adapterPosition)
            }
        }
        showShareButtonLoading(showShareLoadingProgressbar)
    }

    private fun showShareButtonLoading(
        show: Boolean
    ) {
        if (show) {
            binding.shareMaterialButton.text = ""
            binding.shareProgressbar.visibility = View.VISIBLE
        } else {
            binding.shareMaterialButton.text = itemView.resources.getString(R.string.share)
            binding.shareProgressbar.visibility = View.GONE
        }
    }

    fun setShareClickListener(callback: (id: Int) -> Unit) {
        onShareClickListener = callback
    }
}