package me.abolfazl.nmock.view.mockEditor

import org.neshan.common.model.LatLng

class MockEditorState(
    val originAddress: String? = null,
    val destinationAddress: String? = null,
    val lineVector: ArrayList<List<LatLng>>? = null
) {

    companion object {
        inline fun build(copy: MockEditorState, block: Builder.() -> Unit) =
            Builder(copy)
                .applyCopy()
                .apply(block)
                .build()

        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    private constructor(builder: Builder) : this(
        originAddress = builder.originAddress,
        destinationAddress = builder.destinationAddress,
        lineVector = builder.lineVector
    )

    class Builder(
        private val copy: MockEditorState? = null
    ) {
        var originAddress: String? = null
        var destinationAddress: String? = null
        var lineVector: ArrayList<List<LatLng>>? = null

        fun applyCopy(): Builder {
            if (copy == null)
                throw IllegalArgumentException("You call applyCopy, but you don't pass the copy object!")
            this.originAddress = copy.originAddress
            this.destinationAddress = copy.destinationAddress
            this.lineVector = copy.lineVector
            return this
        }

        fun build() = MockEditorState(this)
    }
}
