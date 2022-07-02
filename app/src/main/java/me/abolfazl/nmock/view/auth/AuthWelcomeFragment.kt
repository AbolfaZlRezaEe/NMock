package me.abolfazl.nmock.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentAuthWelcomeBinding

class AuthWelcomeFragment : Fragment() {

    private var _binding: FragmentAuthWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeListeners()
    }

    private fun initializeListeners() {
        binding.signInMaterialButton.setOnClickListener {
            findNavController().navigate(R.id.action_authWelcomeFragment_to_signInFragment)
        }

        binding.signUpMaterialButton.setOnClickListener {
            findNavController().navigate(R.id.action_authWelcomeFragment_to_signUpFragment)
        }
    }
}