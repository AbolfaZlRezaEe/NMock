package me.abolfazl.nmock.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentSignInBinding
import me.abolfazl.nmock.utils.isValidEmail

class SignInFragment : Fragment() {

    companion object {
        private const val MINIMUM_PASSWORD_LENGTH = 8
    }

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeListeners()
    }

    private fun initializeListeners() {
        binding.signInMaterialButton.setOnClickListener { onSignInButtonClicked() }
    }

    private fun onSignInButtonClicked() {
        if (binding.emailTextInputEditText.text.isNullOrEmpty()) {
            binding.emailTextInputEditText.error = resources.getString(R.string.fieldCanNotBeEmpty)
            return
        }
        if (binding.passwordTextInputEditText.text.isNullOrEmpty()) {
            binding.passwordTextInputEditText.error =
                resources.getString(R.string.fieldCanNotBeEmpty)
            return
        }
        if (!binding.emailTextInputEditText.text?.isValidEmail()!!) {
            binding.emailTextInputEditText.error =
                resources.getString(R.string.yourEmailFormatIsNotCorrect)
            return
        }
        if (binding.passwordTextInputEditText.text?.length!! < MINIMUM_PASSWORD_LENGTH) {
            binding.passwordTextInputEditText.error =
                resources.getString(R.string.minimumPasswordError)
            return
        }

        // todo: process...
    }
}