package me.abolfazl.nmock.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
}