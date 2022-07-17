package me.abolfazl.nmock.view.mockImport.fileModels

data class MockFilePositions(
    val id: Long? = null,
    val mockId: Long,
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val elapsedRealTime: Long,
)