package me.abolfazl.nmock.view.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentHomeBinding
import me.abolfazl.nmock.utils.showSnackBar
import me.abolfazl.nmock.view.archive.MockArchiveActivity
import me.abolfazl.nmock.view.editor.MockEditorActivity

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
        initializeListeners(view)
    }

    private fun initializeListeners(view: View) {
        binding.createMockMaterialButton.setOnClickListener {
            activity?.startActivity(Intent(activity, MockEditorActivity::class.java))
        }

        binding.mockListMaterialButton.setOnClickListener {
            activity?.startActivity(Intent(activity, MockArchiveActivity::class.java))
        }

        binding.mockTrackerMaterialButton.setOnClickListener {
            showSnackBar(
                message = resources.getString(R.string.comingSoon),
                rootView = view.findViewById(R.id.homeFragmentRootView),
                duration = Snackbar.LENGTH_LONG
            )
        }

        binding.mockImportMaterialButton.setOnClickListener {
            showSnackBar(
                message = resources.getString(R.string.comingSoon),
                rootView = view.findViewById(R.id.homeFragmentRootView),
                duration = Snackbar.LENGTH_LONG
            )
        }
    }
}