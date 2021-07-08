package com.superology.guestorganizer.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.list.ListItemActionListener
import com.superology.guestorganizer.R
import com.superology.guestorganizer.data.models.Element
import com.superology.guestorganizer.list.viewholders.ElementViewHolder

class ListAdapter(private val itemActionListener: ListItemActionListener<Element>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ModeType {
        READ_ONLY,
        EDIT_ON_CLICK
    }

    var mode = ModeType.READ_ONLY
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var deletedItem: Pair<Int, Element>? = null
    var canUndoDeletedItem = false
    private val items = mutableListOf<Element>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ElementViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list, parent, false)
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ElementViewHolder).bind(items[position], mode, itemActionListener)
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = position.toLong()

    fun update(input: List<Element>) {
        items.clear()
        items.addAll(input)
        notifyDataSetChanged()
    }

    fun deleteItemWithUndo(position: Int) {
        if (items.size >= position) {
            deletedItem = Pair(position, items[position])
            items.removeAt(position)
            canUndoDeletedItem = true
            notifyItemRemoved(position)
        }
    }

    fun undoDeleteItem() {
        if (canUndoDeletedItem) {
            deletedItem?.run {
                items.add(first, second)
                notifyItemInserted(first)
            }
            canUndoDeletedItem = false
            deletedItem = null
        }
    }

    fun getItem(position: Int) = items[position]
}