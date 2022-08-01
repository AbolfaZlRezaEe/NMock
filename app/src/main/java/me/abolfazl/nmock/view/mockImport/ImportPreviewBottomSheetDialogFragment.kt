package me.abolfazl.nmock.view.mockImport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carto.core.ScreenPos
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentImportPreviewBinding
import me.abolfazl.nmock.repository.mock.models.MockDataClass
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

        initListeners()

        initObservers()
    }

    private fun initListeners() {
        binding.openInEditorMaterialButton.setOnClickListener { onOpenInEditorButtonClicked() }

        binding.saveMaterialButton.setOnClickListener { onSaveButtonClicked() }
    }

    private fun onSaveButtonClicked() {
        // todo: save the information with viewmodel
    }

    private fun onOpenInEditorButtonClicked() {
        // todo: open the editor activity and pass the data
    }

    private fun initObservers() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.importMockState.collect { state ->
                state.mockImportedInformation?.let {
                    it.ifNotHandled { mockInformation -> showMockInformationPreview(mockInformation) }
                }
            }
        }
    }

    private fun showMockInformationPreview(
        mockInformation: MockDataClass
    ) {
        if (context == null || mockInformation.lineVector == null) {
            dismiss()
            return
        }

        binding.loadingProgressbar.visibility = View.VISIBLE

        if (mockInformation.name.isNotEmpty()) {
            binding.titleTextInputEditText.setText(mockInformation.name)
        }

        binding.mapview.isEnabled = false
        val originMarker = MarkerManager.createMarker(
            location = mockInformation.originLocation,
            drawableRes = R.drawable.ic_origin_marker,
            context = context,
            elementId = MarkerManager.ELEMENT_ID_ORIGIN_MARKER
        )
        val destinationMarker = MarkerManager.createMarker(
            location = mockInformation.destinationLocation,
            drawableRes = R.drawable.ic_destination_marker,
            context = context,
            elementId = MarkerManager.ELEMENT_ID_DESTINATION_MARKER
        )
        LineManager.drawLineOnMap(
            mapView = binding.mapview,
            vector = mockInformation.lineVector,
        )
        binding.mapview.addMarker(originMarker)
        binding.mapview.addMarker(destinationMarker)

        CameraManager.moveCameraToTripLine(
            mapView = binding.mapview,
            screenPos = ScreenPos(
                binding.mapview.x + 32.toPixel(context!!),
                binding.mapview.y + 32.toPixel(context!!)
            ),
            origin = mockInformation.originLocation,
            destination = mockInformation.destinationLocation
        )

        binding.originAddressTextView.text = mockInformation.originAddress
        binding.destinationAddressTextView.text = mockInformation.destinationAddress

        binding.loadingProgressbar.visibility = View.GONE
    }
}