package me.abolfazl.nmock.view.mockDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import me.abolfazl.nmock.databinding.FragmentMockDetailBinding
import me.abolfazl.nmock.model.database.MockProvider
import me.abolfazl.nmock.model.database.MockType

class MockDetailBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMockDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val KEY_MOCK_INFORMATION_TITLE = "MOCK_TITLE"
        private const val KEY_MOCK_INFORMATION_DESCRIPTION = "MOCK_DESCRIPTION"
        private const val KEY_MOCK_INFORMATION_PROVIDER = "MOCK_PROVIDER"
        private const val KEY_MOCK_INFORMATION_TYPE = "MOCK_TYPE"
        private const val KEY_MOCK_INFORMATION_CREATED_AT = "MOCK_CREATED_AT"
        private const val KEY_MOCK_INFORMATION_UPDATED_AT = "MOCK_UPDATED_AT"

        fun newInstance(
            title: String,
            description: String,
            @MockProvider provider: String,
            @MockType type: String,
            createdAt: String,
            updatedAt: String
        ): MockDetailBottomSheetDialogFragment {
            return MockDetailBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_MOCK_INFORMATION_TITLE, title)
                    putString(KEY_MOCK_INFORMATION_DESCRIPTION, description)
                    putString(KEY_MOCK_INFORMATION_PROVIDER, provider)
                    putString(KEY_MOCK_INFORMATION_TYPE, type)
                    putString(KEY_MOCK_INFORMATION_CREATED_AT, createdAt)
                    putString(KEY_MOCK_INFORMATION_UPDATED_AT, updatedAt)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMockDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewFromBundleInformation()
    }

    private fun initViewFromBundleInformation() {
        arguments?.let { bundle ->
            val title = bundle.getString(KEY_MOCK_INFORMATION_TITLE)
            val description = bundle.getString(KEY_MOCK_INFORMATION_DESCRIPTION)
            val provider = bundle.getString(KEY_MOCK_INFORMATION_PROVIDER)
            val type = bundle.getString(KEY_MOCK_INFORMATION_TYPE)
            val createdAt = bundle.getString(KEY_MOCK_INFORMATION_CREATED_AT)
            val updatedAt = bundle.getString(KEY_MOCK_INFORMATION_UPDATED_AT)

            if (title == null
                || description == null
                || provider == null
                || type == null
                || createdAt == null
                || updatedAt == null
            ) {
                this.dismiss()
                return
            }

            binding.titleTextView.text = title
            binding.descriptionTextView.text = description
            binding.providerTextView.text = provider
            binding.mockTypeTextView.text = type
            binding.updatedAtTextView.text = updatedAt
            binding.createAtTextView.text = createdAt
        }
    }
}