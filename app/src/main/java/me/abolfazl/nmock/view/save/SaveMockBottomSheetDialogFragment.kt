package me.abolfazl.nmock.view.save

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentSaveMockBinding
import me.abolfazl.nmock.utils.Constant

class SaveMockBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSaveMockBinding? = null
    private val binding get() = _binding!!

    private var mockCallback: SaveMockCallback? = null

    companion object {
        private const val KEY_SAVE_MOCK_NAME = "KEY_MOCK_NAME"
        private const val KEY_SAVE_MOCK_DESCRIPTION = "KEY_MOCK_DESCRIPTION"
        private const val KEY_SAVE_MOCK_SPEED = "KEY_MOCK_SPEED"

        fun newInstance(
            name: String? = null,
            description: String? = null,
            speed: String? = null
        ): SaveMockBottomSheetDialogFragment {
            return SaveMockBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_SAVE_MOCK_NAME, name)
                    putString(KEY_SAVE_MOCK_DESCRIPTION, description)
                    putString(KEY_SAVE_MOCK_SPEED, speed)
                }
            }
        }
    }

    fun setMockCallback(
        mockCallback: SaveMockCallback
    ) {
        this.mockCallback = mockCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaveMockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewsFromArgument()

        initListeners()
    }

    private fun initViewsFromArgument() {
        arguments?.let { bundle ->
            val name = bundle.getString(KEY_SAVE_MOCK_NAME)
            val description = bundle.getString(KEY_SAVE_MOCK_DESCRIPTION)
            val speed = bundle.getString(KEY_SAVE_MOCK_SPEED)

            if (name == null || description == null || speed == null) return

            binding.mockNameTextInputEditText.setText(name)
            binding.mockDescriptionTextInputEditText.setText(description)
            binding.speedTextInputEditText.setText(speed)
        }
    }

    private fun initListeners() {
        binding.closeAppCompatImageView.setOnClickListener { mockCallback?.onClose() }
        binding.saveMaterialButton.setOnClickListener(this::onSaveButtonClick)
        binding.speedTextInputEditText.doOnTextChanged { text, _, _, _ ->
            if (text?.length!! > 3) {
                binding.speedTextInputLayout.error = getString(R.string.unValidSpeed)
            } else
                binding.speedTextInputLayout.isErrorEnabled = false
        }
    }

    private fun onSaveButtonClick(view: View) {
        if (binding.mockNameTextInputEditText.text.isNullOrEmpty()) {
            binding.mockNameTextInputLayout.error = getString(R.string.youMustHaveANameForIt)
            return
        }
        if (binding.speedTextInputEditText.text.isNullOrEmpty()) {
            binding.speedTextInputLayout.error = getString(R.string.speedError)
            return
        }
        if (binding.speedTextInputEditText.text!!.length > 3) {
            binding.speedTextInputLayout.error = getString(R.string.unValidSpeed)
            return
        }
        binding.saveMaterialButton.text = ""
        binding.loadingProgressbar.visibility = View.VISIBLE

        mockCallback?.onSave(
            mockName = binding.mockNameTextInputEditText.text!!.toString(),
            mockDescription = binding.mockDescriptionTextInputEditText.text?.toString(),
            speed = binding.speedTextInputEditText.text!!.toString().toInt()
        )
    }

}