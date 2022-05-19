package me.abolfazl.nmock.view.archive

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityMockArchiveBinding
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.utils.isServiceStillRunning
import me.abolfazl.nmock.utils.showSnackBar
import me.abolfazl.nmock.view.dialog.NMockDialog
import me.abolfazl.nmock.view.editor.MockEditorActivity
import me.abolfazl.nmock.view.player.MockPlayerActivity
import me.abolfazl.nmock.view.player.MockPlayerService

@AndroidEntryPoint
class MockArchiveActivity : AppCompatActivity() {

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
                viewModel.mockArchiveState.collect { processState(it) }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.oneTimeEmitter.collect { processAction() }
        }
    }

    private fun initItems(
        list: List<MockDataClass>
    ) {
        if (adapter == null) {
            adapter = MockArchiveAdapter(ArrayList(list), { onClickData ->
                startActivity(
                    Intent(
                        this,
                        MockPlayerActivity::class.java
                    ).also {
                        it.putExtra(MockPlayerActivity.KEY_MOCK_ID_PLAYER, onClickData.id)
                    })
            }) { onLongClickData ->
                startActivity(
                    Intent(
                        this@MockArchiveActivity, MockEditorActivity::class.java
                    ).apply {
                        putExtra(MockEditorActivity.KEY_MOCK_INFORMATION, onLongClickData.id)
                    }
                )
            }
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

    private fun processState(mockArchiveState: MockArchiveState) {
        mockArchiveState.mockList?.let { mockList ->
            binding.contentRecyclerView.visibility =
                if (mockList.isEmpty()) View.GONE else View.VISIBLE
            binding.emptyStateTextView.visibility =
                if (mockList.isEmpty()) View.VISIBLE else View.GONE
            binding.deleteAllImageView.visibility =
                if (mockList.size >= 2) View.VISIBLE else View.GONE
            binding.loadingState.visibility = View.GONE
            initItems(mockList)
        }
    }

    private fun processAction() {
        binding.loadingState.visibility = View.GONE
        showSnackBar(
            message = getString(R.string.unknownException),
            rootView = findViewById(R.id.mockArchiveRootView),
            Snackbar.LENGTH_SHORT
        )
    }

    private fun onDeleteAllClicked() {
        val dialog = NMockDialog.newInstance(
            title = getString(R.string.deleteAllDialogTitle),
            actionButtonText = getString(R.string.yes),
            secondaryButtonText = getString(R.string.cancel)
        )
        dialog.isCancelable = false
        dialog.setDialogListener(
            onActionButtonClicked = {
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