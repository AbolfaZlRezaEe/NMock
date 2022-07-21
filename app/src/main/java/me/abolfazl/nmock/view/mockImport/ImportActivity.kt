package me.abolfazl.nmock.view.mockImport

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityImportBinding
import me.abolfazl.nmock.utils.showSnackBar

class ImportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportBinding

    private val viewModel: ImportMockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners() {
        binding.importFromJsonFileMaterialButton.setOnClickListener {

        }

        binding.importWithOriginAndDestinationLocationMaterialButton.setOnClickListener {
            showSnackBar(
                message = resources.getString(R.string.comingSoon),
                rootView = binding.root,
                duration = Snackbar.LENGTH_LONG
            )
        }

        binding.importWithShareLinkMaterialButton.setOnClickListener {
            showSnackBar(
                message = resources.getString(R.string.comingSoon),
                rootView = binding.root,
                duration = Snackbar.LENGTH_LONG
            )
        }
    }
}