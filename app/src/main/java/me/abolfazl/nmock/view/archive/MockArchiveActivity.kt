package me.abolfazl.nmock.view.archive

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityMockArchiveBinding
import me.abolfazl.nmock.repository.mock.models.MockDataClass
import me.abolfazl.nmock.utils.response.OneTimeEmitter
import me.abolfazl.nmock.utils.showSnackBar
import me.abolfazl.nmock.view.dialog.NMockDialog
import me.abolfazl.nmock.view.editor.MockEditorActivity
import me.abolfazl.nmock.view.player.MockPlayerActivity
import me.abolfazl.nmock.view.player.MockPlayerService
import java.io.File
import java.net.URLConnection

@AndroidEntryPoint
class MockArchiveActivity : AppCompatActivity() {

    companion object {
        // error messages
        const val UNKNOWN_ERROR_MESSAGE = R.string.unknownException
        const val MOCK_INFORMATION_IS_WRONG_MESSAGE = R.string.mockProblemException
        const val EXPORTING_MOCK_FAILED_MESSAGE = R.string.exportingMockProblem
    }

    private lateinit var binding: ActivityMockArchiveBinding
    private val viewModel: MockArchiveViewModel by viewModels()

    private var adapter: MockArchiveAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initState()

        initListeners()
    }

    override fun onStart() {
        super.onStart()
        binding.loadingState.visibility = View.VISIBLE
        binding.contentRecyclerView.visibility = View.GONE
        viewModel.getMocks()
    }

    private fun initListeners() {
        binding.backImageView.setOnClickListener { this.finish() }

        binding.deleteAllImageView.setOnClickListener { onDeleteAllClicked() }

        binding.addNewMockExtendedFab.setOnClickListener {
            startActivity(Intent(this, MockEditorActivity::class.java))
        }
    }

    private fun initState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mockArchiveState.collect { state ->

                    state.mockList?.let {
                        it.ifNotHandled { mockList -> processMockList(mockList) }
                    }

                    state.filePath?.let {
                        it.ifNotHandled { filePath -> processSharedFilePath(filePath) }
                    }

                    state.sharedMockDataClassState?.let {
                        it.ifNotHandled { mockDataClass ->
                            adapter?.changeTheStateOfShareLoadingProgressbar(
                                mockDataClass = mockDataClass
                            )
                        }
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.oneTimeEmitter.collect { processAction(it) }
        }
    }

    private fun processSharedFilePath(file: File) {
        val shareFileIntent = Intent(Intent.ACTION_SEND)
        shareFileIntent.type = URLConnection.guessContentTypeFromName(file.name)
        val uri = FileProvider.getUriForFile(
            this,
            this.applicationContext.packageName,
            file
        )
        shareFileIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(shareFileIntent, null))
    }

    private fun initItems(list: List<MockDataClass>) {
        if (adapter == null) {
            adapter =
                MockArchiveAdapter(
                    ArrayList(list),
                    { onItemClicked(it) },
                    { onItemLongClicked(it) },
                    { viewModel.processExportingMock(it) }
                )
        } else {
            adapter?.updateData(ArrayList(list))
        }
        adapter?.let {
            binding.contentRecyclerView.layoutManager =
                LinearLayoutManager(
                    this,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            binding.contentRecyclerView.adapter = adapter
        }
    }

    private fun onItemClicked(mockDataClass: MockDataClass) {
        if (!MockPlayerService.SERVICE_IS_RUNNING) {
            startPlayer(mockDataClass.id!!, false)
            return
        }
        val dialog = NMockDialog.newInstance(
            title = resources.getString(R.string.youWantToStopLastMock),
            actionButtonText = resources.getString(R.string.yes),
            secondaryButtonText = resources.getString(R.string.no)
        )
        dialog.isCancelable = true
        dialog.setDialogListener(
            onActionButtonClicked = {
                startPlayer(mockDataClass.id!!, true)
                dialog.dismiss()
            },
            onSecondaryButtonClicked = { dialog.dismiss() }
        )
        dialog.show(supportFragmentManager.beginTransaction(), null)
    }

    private fun onItemLongClicked(mockDataClass: MockDataClass) {
        startActivity(
            Intent(
                this@MockArchiveActivity, MockEditorActivity::class.java
            ).apply { putExtra(MockEditorActivity.KEY_MOCK_INFORMATION, mockDataClass.id) }
        )
    }

    private fun startPlayer(mockId: Long, reset: Boolean) {
        startActivity(Intent(this, MockPlayerActivity::class.java).apply {
            if (reset) {
                putExtra(MockPlayerActivity.RESET_COMMAND, true)
            }
            putExtra(MockPlayerActivity.KEY_MOCK_ID_PLAYER, mockId)
        })
    }

    private fun processMockList(
        mockList: List<MockDataClass>
    ) {
        binding.contentRecyclerView.visibility =
            if (mockList.isEmpty()) View.GONE else View.VISIBLE
        binding.emptyStateTextView.visibility =
            if (mockList.isEmpty()) View.VISIBLE else View.GONE
        binding.deleteAllImageView.visibility =
            if (mockList.size >= 2) View.VISIBLE else View.GONE
        binding.loadingState.visibility = View.GONE
        initItems(mockList)
    }

    private fun processAction(response: OneTimeEmitter) {
        binding.loadingState.visibility = View.GONE
        showSnackBar(
            message = resources.getString(response.message),
            rootView = binding.root,
            Snackbar.LENGTH_SHORT
        )
    }

    private fun onDeleteAllClicked() {
        val dialog = NMockDialog.newInstance(
            title = if (MockPlayerService.SERVICE_IS_RUNNING) resources.getString(R.string.youWantStopAndDeleteMock)
            else resources.getString(R.string.deleteAllDialogTitle),
            actionButtonText = resources.getString(R.string.yes),
            secondaryButtonText = resources.getString(R.string.cancel)
        )
        dialog.isCancelable = false
        dialog.setDialogListener(
            onActionButtonClicked = {
                if (MockPlayerService.SERVICE_IS_RUNNING) {
                    startService(Intent(this, MockPlayerService::class.java).apply {
                        putExtra(MockPlayerService.KILL_SERVICE, true)
                    })
                }
                viewModel.deleteAllMocks()
                adapter?.removeAll()
                binding.contentRecyclerView.visibility = View.GONE
                binding.emptyStateTextView.visibility = View.VISIBLE
                dialog.dismiss()
            },
            onSecondaryButtonClicked = {
                dialog.dismiss()
            }
        )
        dialog.show(supportFragmentManager.beginTransaction(), null)
    }
}