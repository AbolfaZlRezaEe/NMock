package me.abolfazl.nmock.view.archive

import me.abolfazl.nmock.repository.normalMock.models.MockDataClass
import me.abolfazl.nmock.utils.response.SingleEvent
import java.io.File

data class MockArchiveState(
    val mockList: SingleEvent<List<MockDataClass>>? = null,
    val file: SingleEvent<File>? = null,
    val sharedMockDataClassState: SingleEvent<MockDataClass>? = null
)
