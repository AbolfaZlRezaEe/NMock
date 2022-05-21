package me.abolfazl.nmock.view.speedDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jem.rubberpicker.RubberSeekBar
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentMockSpeedBinding

class MockSpeedBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMockSpeedBinding? = null
    private val binding get() = _binding!!

    private var callback: ((speed: Int) -> Unit)? = null

    companion object {
        const val KEY_SPEED = "SPEED_KEY"

        fun newInstance(
            speed: Int
        ): MockSpeedBottomSheetDialogFragment {
            return MockSpeedBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_SPEED, speed)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMockSpeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewsFromBundle()

        initListeners()
    }

    private fun initViewsFromBundle() {
        arguments?.let { bundle ->
            val speed = bundle.getInt(KEY_SPEED)
            binding.speedTextInputEditText.setText(speed.toString())
            binding.speedSeekbar.setCurrentValue(speed)
        }
    }

    private fun initListeners() {
        binding.closeAppCompatImageView.setOnClickListener { this.dismiss() }
        binding.speedSeekbar.setOnRubberSeekBarChangeListener(object :
            RubberSeekBar.OnRubberSeekBarChangeListener {
            override fun onProgressChanged(seekBar: RubberSeekBar, value: Int, fromUser: Boolean) {
                if (!fromUser) return
                binding.speedTextInputEditText.setText(value.toString())
            }

            override fun onStartTrackingTouch(seekBar: RubberSeekBar) {}
            override fun onStopTrackingTouch(seekBar: RubberSeekBar) {}
        })

        binding.speedTextInputEditText.doOnTextChanged { text, _, _, _ ->
            if (text?.length!! > 3) {
                binding.speedTextInputLayout.error = resources.getString(R.string.unValidSpeed)
            } else if (text.isNotEmpty()) {
                binding.speedTextInputLayout.isErrorEnabled = false
                binding.speedSeekbar.setCurrentValue(text.toString().toInt())
            } else {
                binding.speedTextInputLayout.error = resources.getString(R.string.speedError)
            }
        }

        binding.saveMaterialButton.setOnClickListener {
            if (binding.speedTextInputEditText.text?.length!! > 3) return@setOnClickListener
            if (binding.speedTextInputEditText.text.isNullOrEmpty()) {
                binding.speedTextInputLayout.error = resources.getString(R.string.speedError)
                return@setOnClickListener
            }
            binding.saveMaterialButton.text = ""
            binding.loadingProgressbar.visibility = View.VISIBLE

            callback?.invoke(binding.speedTextInputEditText.text!!.toString().toInt())
        }
    }

    fun setOnSaveClickListener(callback: (speed: Int) -> Unit) {
        this.callback = callback
    }
}