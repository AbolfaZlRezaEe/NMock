package me.abolfazl.nmock.view.mockArchive

import me.abolfazl.nmock.repository.models.MockDataClass

data class MockArchiveState(
    val mockList: List<MockDataClass>? = null,
)
