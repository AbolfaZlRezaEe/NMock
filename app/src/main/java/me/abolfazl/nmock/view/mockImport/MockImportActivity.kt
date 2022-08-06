package me.abolfazl.nmock.view.mockImport

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityImportMockBinding
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.showSnackBar
import me.abolfazl.nmock.view.dialog.LoadingDialogFragment
import me.abolfazl.nmock.view.dialog.NMockDialog
import me.abolfazl.nmock.view.editor.MockEditorActivity
import me.abolfazl.nmock.view.player.MockPlayerActivity
import me.abolfazl.nmock.view.player.MockPlayerService
import java.io.BufferedReader
import java.io.InputStreamReader

@AndroidEntryPoint
class MockImportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportMockBinding

    private val viewModel: ImportMockViewModel by viewModels()
    private var loadingDialog: LoadingDialogFragment? = null
    private var importPreviewBottomSheet: BottomSheetDialogFragment? = null

    private val importJsonFileResultIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fileUri = result.data?.data
                if (fileUri == null) {
                    showSnackBar(
                        message = resources.getString(R.string.jsonParseProcessMessage),
                        rootView = binding.root,
                        duration = Snackbar.LENGTH_LONG
                    )
                    return@registerForActivityResult
                }
                val jsonTextFile = StringBuilder()
                BufferedReader(InputStreamReader(contentResolver.openInputStream(fileUri)))
                    .use { jsonTextFile.append(it.readLine()) }
                viewModel.parseJsonData(jsonTextFile.toString())
            }
        }

    companion object {
        // error messages
        const val UNKNOWN_ERROR_MESSAGE = R.string.unknownException
        const val JSON_STRUCTURE_PROBLEM_MESSAGE = R.string.jsonStructureProblemMessage
        const val JSON_PARSE_PROCESS_PROBLEM_MESSAGE = R.string.jsonParseProcessMessage
        const val MOCK_INFORMATION_HAS_PROBLEM = R.string.importedMockProblemException
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportMockBinding.inflate(layoutInflater)
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

                    state.finalMockId?.let {
                        it.ifNotHandled { importedMockId -> processImportedMockId(importedMockId) }
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.oneTimeEmitter.collect { processAction(it) }
        }
    }

    private fun processMockImportedInformation() {
        importPreviewBottomSheet = ImportPreviewBottomSheetDialogFragment()
        importPreviewBottomSheet?.let { bottomSheetDialogFragment ->
            bottomSheetDialogFragment.isCancelable = true
            bottomSheetDialogFragment.show(supportFragmentManager, null)
        }
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
        when (response.actionId) {
            ImportMockViewModel.ACTION_MOCK_DOES_NOT_SAVED -> {
                importPreviewBottomSheet?.dismiss()
                importPreviewBottomSheet = null
                showSnackBar(
                    message = resources.getString(response.message),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_LONG
                )
            }
            else -> {
                showSnackBar(
                    message = resources.getString(response.message),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_SHORT
                )
            }
        }
    }

    private fun initListeners() {
        binding.importFromJsonFileMaterialButton.setOnClickListener {
            importJsonFileResultIntent.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            })
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

    private fun processImportedMockId(
        importedMockId: Long
    ) {
        importPreviewBottomSheet?.dismiss()
        importPreviewBottomSheet = null
        if (viewModel.importMockState.value.shouldOpenOnEditor) {
            startActivity(
                Intent(
                    this, MockEditorActivity::class.java
                ).apply {
                    putExtra(MockEditorActivity.KEY_MOCK_INFORMATION, importedMockId)
                    putExtra(MockEditorActivity.KEY_MOCK_IS_IMPORTED, true)
                }
            )
        } else {
            suggestPlayerDialogToUser(importedMockId)
        }
    }

    private fun suggestPlayerDialogToUser(
        importedMockId: Long
    ) {
        if (MockPlayerService.SERVICE_IS_RUNNING) return
        val dialog = NMockDialog.newInstance(
            title = resources.getString(R.string.playingMockDialogTitle),
            actionButtonText = resources.getString(R.string.yes),
            secondaryButtonText = resources.getString(R.string.no)
        )
        dialog.isCancelable = false
        dialog.setDialogListener(
            onActionButtonClicked = {
                startActivity(
                    Intent(
                        this,
                        MockPlayerActivity::class.java
                    ).also {
                        it.putExtra(
                            MockPlayerActivity.KEY_MOCK_ID_PLAYER,
                            importedMockId
                        )
                        it.putExtra(MockPlayerActivity.KEY_MOCK_IS_IMPORTED, true)
                    })
                dialog.dismiss()
                this.finish()
            },
            onSecondaryButtonClicked = {
                dialog.dismiss()
                viewModel.clearImportMockState()
            }
        )
        dialog.show(supportFragmentManager.beginTransaction(), null)
    }
}