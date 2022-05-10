package me.abolfazl.nmock.view.save

interface SaveMockCallback {

    fun onClose()

    fun onSave(
        mockName: String,
        mockDescription: String?,
        speed: Int
    )
}