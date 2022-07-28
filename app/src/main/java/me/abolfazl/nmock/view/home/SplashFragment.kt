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
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentSplashBinding
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
        Handler(Looper.getMainLooper())
            .postDelayed({
                if (authViewModel.isUserLoggedIn()) {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                } else {
                    startActivity(Intent(requireActivity(), AuthActivity::class.java))
                    requireActivity().finish()
                }
            }, 3000)
    }
}