package me.abolfazl.nmock.view.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import me.abolfazl.nmock.BuildConfig
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentHomeBinding
import me.abolfazl.nmock.utils.logger.NMockLogger
import me.abolfazl.nmock.utils.showSnackBar
import me.abolfazl.nmock.view.archive.MockArchiveActivity
import me.abolfazl.nmock.view.editor.MockEditorActivity
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var logger: NMockLogger

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

        initializeVersionName()

        initializeListeners()
    }

    private fun initializeVersionName(){
        val beforeVersionText =
            if (BuildConfig.DEBUG) resources.getString(R.string.debug) else resources.getString(R.string.release)

        binding.versionCode.text =
            "$beforeVersionText ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
    }

    private fun initializeListeners() {
        binding.createMockMaterialButton.setOnClickListener {
            activity?.startActivity(Intent(activity, MockEditorActivity::class.java))
        }

        binding.mockListMaterialButton.setOnClickListener {
            activity?.startActivity(Intent(activity, MockArchiveActivity::class.java))
        }

        binding.mockTrackerMaterialButton.setOnClickListener {
            showSnackBar(
                message = resources.getString(R.string.comingSoon),
                rootView = binding.root,
                duration = Snackbar.LENGTH_LONG
            )
        }

        binding.mockImportMaterialButton.setOnClickListener {
            showSnackBar(
                message = resources.getString(R.string.comingSoon),
                rootView = binding.root,
                duration = Snackbar.LENGTH_LONG
            )
        }

        binding.reportLogsMaterialButton.setOnClickListener {
            logger.sendLogsFile()
            showSnackBar(
                message = resources.getString(R.string.thankYouForYourHelp),
                rootView = binding.root,
                duration = Snackbar.LENGTH_SHORT
            )
        }
    }
}