package me.abolfazl.nmock.view.mockImport

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityImportBinding
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.showSnackBar
import me.abolfazl.nmock.view.dialog.LoadingDialogFragment

@AndroidEntryPoint
class ImportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportBinding

    private val viewModel: ImportMockViewModel by viewModels()
    private var loadingDialog: LoadingDialogFragment? = null

    companion object {
        // error messages
        const val UNKNOWN_ERROR_MESSAGE = R.string.unknownException
        const val JSON_STRUCTURE_PROBLEM_MESSAGE = R.string.jsonStructureProblemMessage
        const val JSON_PARSE_PROCESS_PROBLEM_MESSAGE = R.string.jsonParseProcessMessage
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initObservers()

        initListeners()
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.importMockState.collect { state ->
                    state.showImportLoading.ifNotHandled { processShowingLoadingState(it) }

                    state.mockImportedInformation?.let { mockImportedInformation ->
                        mockImportedInformation.ifNotHandled { processMockImportedInformation() }
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.oneTimeEmitter.collect { processAction(it) }
        }
    }

    private fun processMockImportedInformation() {
        val importedPreview = ImportPreviewBottomSheetDialogFragment()
        importedPreview.isCancelable = true
        importedPreview.show(supportFragmentManager, null)
    }

    private fun processShowingLoadingState(
        show: Boolean
    ) {
        if (show) {
            loadingDialog = LoadingDialogFragment.newInstance()
            loadingDialog?.let { dialog ->
                dialog.isCancelable = false
                dialog.show(supportFragmentManager, null)
            }
        } else {
            loadingDialog?.dismiss()
            loadingDialog = null
        }
    }

    private fun processAction(
        response: OneTimeEmitter
    ) {
        showSnackBar(
            message = resources.getString(response.message),
            rootView = binding.root,
            duration = Snackbar.LENGTH_SHORT
        )
    }

    private fun initListeners() {
        binding.importFromJsonFileMaterialButton.setOnClickListener {
            //todo: open explorer for choosing file
            //todo: pass the json data to viewModel
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