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

    private var mockName: String? = null
    private var mockDescription: String? = null
    private var mockCallback: SaveMockCallback? = null

    companion object {
        fun newInstance(
            mockName: String? = null,
            mockDescription: String? = null
        ): SaveMockBottomSheetDialogFragment {
            return SaveMockBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(Constant.KEY_SAVE_MOCK_NAME, mockName)
                    putString(Constant.KEY_SAVE_MOCK_DESCRIPTION, mockDescription)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getArgumentsFromBundle()
    }

    private fun getArgumentsFromBundle() {
        arguments?.let { bundle ->
            this.mockName = bundle.getString(Constant.KEY_SAVE_MOCK_NAME)
            this.mockDescription = bundle.getString(Constant.KEY_SAVE_MOCK_DESCRIPTION)
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
        initListeners()
    }

    private fun initListeners() {
        binding.closeAppCompatImageView.setOnClickListener { mockCallback?.onClose() }
        binding.cancelMaterialButton.setOnClickListener { mockCallback?.onClose() }
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