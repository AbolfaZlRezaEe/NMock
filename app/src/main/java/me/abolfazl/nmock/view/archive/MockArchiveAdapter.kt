package me.abolfazl.nmock.view.archive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.abolfazl.nmock.databinding.ItemMockArchiveBinding
import me.abolfazl.nmock.repository.models.MockDataClass
import me.abolfazl.nmock.utils.setupListeners

class MockArchiveAdapter constructor(
    private val list: ArrayList<MockDataClass>,
    private val onClick: (mockDataClass: MockDataClass) -> Unit,
    private val onLongClick: (mockDataClass: MockDataClass) -> Unit
) : RecyclerView.Adapter<MockArchiveViewHolder>() {

    fun updateData(
        list: List<MockDataClass>
    ) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
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
        return MockArchiveViewHolder(
            ItemMockArchiveBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).setupListeners(
            { longClickPosition -> onLongClick.invoke(list[longClickPosition]) },
            { onClickPosition -> onClick.invoke(list[onClickPosition]) }
        )
    }

    override fun onBindViewHolder(holder: MockArchiveViewHolder, position: Int) {
        val mockObject = list[position]
        holder.bind(
            title = mockObject.name,
            mockProvider = mockObject.provider,
            speed = mockObject.speed,
            originLocation = mockObject.originLocation,
            destinationLocation = mockObject.destinationLocation,
            lineVector = mockObject.lineVector
        )

    }

    override fun getItemCount(): Int {
        return list.size
    }

}