package me.abolfazl.nmock.view.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentSplashBinding
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.view.auth.AuthActivity
import me.abolfazl.nmock.view.auth.AuthViewModel

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObservers()

        Handler(Looper.getMainLooper())
            .postDelayed({
                lifecycleScope.launch { authViewModel.isUserLoggedIn() }
            }, 2000)
    }

    private fun initObservers() {
        lifecycleScope.launchWhenCreated {
            authViewModel.oneTimeEmitter.collect { processAction(it) }
        }
    }

    private fun processAction(
        response: OneTimeEmitter
    ) {
        if (response.actionId == AuthViewModel.ACTION_USER_LOGGED_IN) {
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
        } else if (response.actionId == AuthViewModel.ACTION_USER_DOES_NOT_LOGGED_IN) {
            startActivity(Intent(requireActivity(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }
}