package me.abolfazl.nmock.view.mockImport

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carto.core.ScreenPos
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentImportPreviewBinding
import me.abolfazl.nmock.repository.mock.models.viewModels.MockDataClass
import me.abolfazl.nmock.utils.managers.CameraManager
import me.abolfazl.nmock.utils.managers.LineManager
import me.abolfazl.nmock.utils.managers.MarkerManager
import me.abolfazl.nmock.utils.toPixel

@AndroidEntryPoint
class ImportPreviewBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentImportPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ImportMockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showBottomSheetFullyExpanded()

        initListeners()

        initObservers()

        showMockInformationPreview(viewModel.importMockState.value.mockImportedInformation?.getRawValue())
    }

    private fun showBottomSheetFullyExpanded() {
        val dialog = dialog as BottomSheetDialog
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.importMockState.collect { state ->
                    state.showSaveLoading.ifNotHandled { showLoading(it) }
                }
            }
        }
    }

    private fun initListeners() {
        /* We save the mock first in the database for passing that to MockEditorActivity and
           We read that in MockEditorActivity! */
        binding.openInEditorMaterialButton.setOnClickListener {
            viewModel.setImportedMockTitle(title = binding.titleTextInputEditText.text?.toString())
            viewModel.saveMockInformation(true)
        }

        binding.saveMaterialButton.setOnClickListener {
            viewModel.setImportedMockTitle(title = binding.titleTextInputEditText.text?.toString())
            viewModel.saveMockInformation(false)
        }
    }

    private fun showMockInformationPreview(
        mockDataClass: MockDataClass?
    ) {
        if (mockDataClass == null || context == null || mockDataClass.lineVector == null) {
            dismiss()
            return
        }

        showLoading(true)

        if (mockDataClass.name.isNotEmpty()) {
            binding.titleTextInputEditText.setText(mockDataClass.name)
        }

        val originMarker = MarkerManager.createMarker(
            location = mockDataClass.originLocation,
            drawableRes = R.drawable.ic_origin_marker,
            context = context,
            elementId = MarkerManager.ELEMENT_ID_ORIGIN_MARKER
        )
        val destinationMarker = MarkerManager.createMarker(
            location = mockDataClass.destinationLocation,
            drawableRes = R.drawable.ic_destination_marker,
            context = context,
            elementId = MarkerManager.ELEMENT_ID_DESTINATION_MARKER
        )
        LineManager.drawLineOnMap(
            mapView = binding.mapview,
            vector = mockDataClass.lineVector!!,
        )
        binding.mapview.addMarker(originMarker)
        binding.mapview.addMarker(destinationMarker)

        binding.originAddressTextView.text = mockDataClass.originAddress
        binding.destinationAddressTextView.text = mockDataClass.destinationAddress

        Handler(Looper.getMainLooper()).postDelayed({
            CameraManager.moveCameraToTripLine(
                mapView = binding.mapview,
                screenPos = ScreenPos(
                    binding.mapview.x - 20.toPixel(context!!),
                    binding.mapview.y - 20.toPixel(context!!)
                ),
                origin = mockDataClass.originLocation,
                destination = mockDataClass.destinationLocation
            )
        }, 500)

        showLoading(false)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.loadingProgressbar.visibility = View.VISIBLE
            binding.saveMaterialButton.text = ""
        } else {
            binding.loadingProgressbar.visibility = View.GONE
            binding.saveMaterialButton.text = resources.getString(R.string.save)
        }
    }
}