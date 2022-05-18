package me.abolfazl.nmock.view.archive

import me.abolfazl.nmock.repository.models.MockDataClass

data class MockArchiveState(
    val mockList: List<MockDataClass>? = null,
)
