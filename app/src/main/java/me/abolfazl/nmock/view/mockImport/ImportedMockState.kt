package me.abolfazl.nmock.view.mockImport

import me.abolfazl.nmock.repository.mock.models.MockImportedDataClass
import me.abolfazl.nmock.utils.response.SingleEvent

data class ImportedMockState(
    val showImportLoading: SingleEvent<Boolean> = SingleEvent(false),
    val showSaveLoading: SingleEvent<Boolean> = SingleEvent(false),
    val mockImportedInformation: SingleEvent<MockImportedDataClass>? = null,
    val finalMockId: SingleEvent<Long>? = null,
    val shouldOpenOnEditor: Boolean = false
)
