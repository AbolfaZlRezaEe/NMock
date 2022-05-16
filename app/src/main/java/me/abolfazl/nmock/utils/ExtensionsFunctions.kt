package me.abolfazl.nmock.utils

import androidx.recyclerview.widget.RecyclerView

fun <T : RecyclerView.ViewHolder> T.setupListeners(
    onLongClick: (position: Int) -> Unit,
    onClick: (position: Int) -> Unit
): T {
    itemView.setOnLongClickListener {
        onLongClick.invoke(adapterPosition)
        return@setOnLongClickListener false
    }
    itemView.setOnClickListener {
        onClick.invoke(adapterPosition)
    }
    return this
}