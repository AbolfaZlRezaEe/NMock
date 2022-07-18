package me.abolfazl.nmock.view.archive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.abolfazl.nmock.databinding.ItemMockArchiveBinding
import me.abolfazl.nmock.repository.mock.models.MockDataClass
import me.abolfazl.nmock.utils.setupListeners

class MockArchiveAdapter constructor(
    private val list: ArrayList<MockDataClass>,
    private val onItemClickListener: (mockDataClass: MockDataClass) -> Unit,
    private val onItemLongClickListener: (mockDataClass: MockDataClass) -> Unit,
    private val onShareButtonClickListener: (mockDataClass: MockDataClass) -> Unit
) : RecyclerView.Adapter<MockArchiveViewHolder>() {

    fun updateData(
        list: List<MockDataClass>
    ) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun changeTheStateOfShareLoadingProgressbar(
        mockDataClass: MockDataClass,
    ) {
        list.find { it.id == mockDataClass.id }?.showShareLoading = mockDataClass.showShareLoading
        notifyItemChanged(list.indexOf(mockDataClass))
    }

    fun addNewItem(
        mockDataClass: MockDataClass
    ) {
        this.list.add(mockDataClass)
        notifyItemInserted(list.size - 1)
    }

    fun removeItem(
        mockDataClass: MockDataClass
    ) {
        val index = list.indexOf(mockDataClass)
        if (index == -1) return
        list.removeAt(index)
        notifyItemRemoved(index)
    }

    fun removeAll() {
        list.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MockArchiveViewHolder {
        val mockArchiveViewHolder = MockArchiveViewHolder(
            ItemMockArchiveBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).setupListeners(
            { longClickPosition -> onItemLongClickListener.invoke(list[longClickPosition]) },
            { clickPosition -> onItemClickListener.invoke(list[clickPosition]) }
        )
        mockArchiveViewHolder.setShareClickListener { shareClickPosition ->
            onShareButtonClickListener.invoke(list[shareClickPosition])
        }
        return mockArchiveViewHolder
    }

    override fun onBindViewHolder(holder: MockArchiveViewHolder, position: Int) {
        val mockObject = list[position]
        holder.bind(
            title = mockObject.name,
            mockProvider = mockObject.provider,
            speed = mockObject.speed,
            description = mockObject.description,
            showShareLoadingProgressbar = mockObject.showShareLoading
        )

    }

    override fun getItemCount(): Int {
        return list.size
    }

}