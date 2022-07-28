package me.abolfazl.nmock.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentSignInBinding
import me.abolfazl.nmock.utils.isValidEmail
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.showSnackBar

@AndroidEntryPoint
class SignInFragment : Fragment() {

    companion object {
        private const val MINIMUM_PASSWORD_LENGTH = 8

        // Error Messages
        const val EMAIL_OR_PASSWORD_IS_NOT_VALID_MESSAGE = R.string.emailOrPasswordIsWrongMessage
        const val SIGNIN_PROCESS_FAILED_MESSAGE = R.string.authExceptionMessage
    }

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

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

        initObservers()
    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.oneTimeEmitter.collect { processAction(it) }
        }
    }

    private fun processAction(response: OneTimeEmitter) {
        showLoading(false)

        when (response.actionId) {
            AuthViewModel.ACTION_AUTH_SUCCESSFULLY -> {
                findNavController().navigate(R.id.action_signInFragment_to_authSuccessFragment)
            }
            else -> {
                showSnackBar(
                    message = resources.getString(response.message),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_LONG
                )
            }
        }
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

        showLoading(true)

        viewModel.signIn(
            email = binding.emailTextInputEditText.text?.toString()!!,
            password = binding.passwordTextInputEditText.text?.toString()!!
        )
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.signInMaterialButton.isEnabled = false
            binding.loadingLinearLayout.visibility = View.VISIBLE
        } else {
            binding.signInMaterialButton.isEnabled = true
            binding.loadingLinearLayout.visibility = View.GONE
        }
    }
}