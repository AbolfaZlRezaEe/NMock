package me.abolfazl.nmock.view.mockDialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import me.abolfazl.nmock.databinding.FragmentDialogBinding

class NMockDialog : DialogFragment() {

    private var _binding: FragmentDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var onActionButtonClicked: () -> Unit
    private lateinit var onSecondaryButtonClicked: () -> Unit

    companion object {
        private const val DIALOG_KEY_TITLE = "DIALOG_TITLE"
        private const val DIALOG_KEY_ACTION_TEXT = "ACTION_TEXT"
        private const val DIALOG_KEY_SECONDARY_TEXT = "SECONDARY_TEXT"

        fun newInstance(
            title: String,
            actionButtonText: String,
            secondaryButtonText: String
        ): NMockDialog {
            val bundle = Bundle().apply {
                putString(DIALOG_KEY_TITLE, title)
                putString(DIALOG_KEY_ACTION_TEXT, actionButtonText)
                putString(DIALOG_KEY_SECONDARY_TEXT, secondaryButtonText)
            }
            return NMockDialog().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // for removing default background of dialog
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)

        initializeView()
        initListeners()
    }

    private fun initializeView() {
        arguments?.let { bundle ->
            val title = bundle.getString(DIALOG_KEY_TITLE)
            val actionButtonText = bundle.getString(DIALOG_KEY_ACTION_TEXT)
            val secondaryButtonText = bundle.getString(DIALOG_KEY_SECONDARY_TEXT)

            binding.titleTextView.text = title
            binding.actionMaterialButton.text = actionButtonText
            binding.secondryMaterialButton.text = secondaryButtonText
        }
    }

    private fun initListeners() {
        binding.actionMaterialButton.setOnClickListener { onActionButtonClicked.invoke() }
        binding.secondryMaterialButton.setOnClickListener { onSecondaryButtonClicked.invoke() }
    }

    fun setDialogListener(
        onActionButtonClicked: () -> Unit,
        onSecondaryButtonClicked: () -> Unit
    ) {
        this.onActionButtonClicked = onActionButtonClicked
        this.onSecondaryButtonClicked = onSecondaryButtonClicked
    }

}