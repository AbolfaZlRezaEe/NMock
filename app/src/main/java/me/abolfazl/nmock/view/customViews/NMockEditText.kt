package me.abolfazl.nmock.view.customViews

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import me.abolfazl.nmock.R

class NMockEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        initView()
    }

    private fun initView() {
        inflate(context, R.layout.custom_edit_text, this)
    }
}