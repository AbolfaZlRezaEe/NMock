package me.abolfazl.nmock.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentLoadingDialogBinding

class LoadingDialogFragment : DialogFragment() {

    private var _binding: FragmentLoadingDialogBinding? = null
    private val binding get() = _binding!!

    companion object {

        private const val KEY_LOADING_MESSAGE = "LOADING_MESSAGE"

        fun newInstance(
            loadingMessage: String? = null,
        ): LoadingDialogFragment {
            return LoadingDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_LOADING_MESSAGE, loadingMessage)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        val loadingMessage: String =
            arguments?.getString(KEY_LOADING_MESSAGE, null)
                ?: resources.getString(R.string.pleaseWait)

        binding.progressTextView.text = loadingMessage
    }
}