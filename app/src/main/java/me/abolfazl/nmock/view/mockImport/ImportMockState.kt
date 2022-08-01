package me.abolfazl.nmock.view.mockImport

import me.abolfazl.nmock.repository.mock.models.MockDataClass
import me.abolfazl.nmock.utils.response.SingleEvent

data class ImportMockState(
    val showImportLoading: SingleEvent<Boolean> = SingleEvent(false),
    val mockImportedInformation: SingleEvent<MockDataClass>? = null,
)
