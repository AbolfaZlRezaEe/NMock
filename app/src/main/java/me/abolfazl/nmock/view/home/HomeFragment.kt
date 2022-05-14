package me.abolfazl.nmock.view.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.abolfazl.nmock.databinding.FragmentHomeBinding
import me.abolfazl.nmock.view.mockArchive.MockArchiveActivity
import me.abolfazl.nmock.view.mockEditor.MockEditorActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeListeners()
    }

    private fun initializeListeners() {
        binding.newTripMaterialButtonHome.setOnClickListener {
            activity?.startActivity(Intent(activity, MockEditorActivity::class.java))
        }

        binding.mockArchiveMaterialButtonHome.setOnClickListener {
            activity?.startActivity(Intent(activity, MockArchiveActivity::class.java))
        }
    }
}