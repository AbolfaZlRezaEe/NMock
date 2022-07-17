package me.abolfazl.nmock.view.mockImport

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityImportBinding
import me.abolfazl.nmock.utils.Constant
import me.abolfazl.nmock.utils.managers.PermissionManager
import me.abolfazl.nmock.utils.showSnackBar
import me.abolfazl.nmock.view.mockImport.fileModels.MockFileModel
import java.io.IOException
import java.io.ObjectInputStream

@AndroidEntryPoint
class ImportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportBinding
    private val viewModel: MockImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeListeners()
    }

    private fun initializeListeners() {
        binding.importMockFileMaterialButton.setOnClickListener { onImportFromMockFileClicked() }
    }

    private fun onImportFromMockFileClicked() {
        if (!managePermissions()) return
        val intent = Intent()
            .setAction(Intent.ACTION_GET_CONTENT)
            .setType("*/*")
        importMockFromFileResult.launch(intent)
    }

    private val importMockFromFileResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                    val inputStream = ObjectInputStream(contentResolver.openInputStream(uri))
                    val mockFileDataclass: MockFileModel =
                        inputStream.readObject() as MockFileModel
                    viewModel.loadMockInformationFromFileImport(mockFileDataclass)
                }
            } catch (exception: IOException) {
                // todo: catch the exception and show an error
            }
        } else {
            // todo: show the user an error
        }
    }


    private fun managePermissions(): Boolean {
        return if (!PermissionManager.storagePermissionIsGranted(this)) {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            val shouldShowRelational = ActivityCompat.shouldShowRequestPermissionRationale(
                this, permission
            )
            if (shouldShowRelational) {
                showSnackBar(
                    message = resources.getString(R.string.storagePermissionRational),
                    rootView = binding.root,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    actionText = resources.getString(R.string.iAccept),
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        PermissionManager.getStoragePermissionList().toTypedArray(),
                        Constant.STORAGE_REQUEST
                    )
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    PermissionManager.getStoragePermissionList().toTypedArray(),
                    Constant.STORAGE_REQUEST
                )
            }
            false
        } else
            true
    }
}