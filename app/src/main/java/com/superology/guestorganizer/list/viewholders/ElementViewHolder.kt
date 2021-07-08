package com.superology.guestorganizer.list.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.list.ListItemActionListener
import com.superology.guestorganizer.R
import com.superology.guestorganizer.data.models.Element
import com.superology.guestorganizer.list.ListAdapter

class ElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val guestView = itemView.findViewById<TextView>(R.id.guestView)
    private val hostView = itemView.findViewById<TextView>(R.id.hostView)
    private val timeView = itemView.findViewById<TextView>(R.id.timeView)
    private val descriptionView = itemView.findViewById<TextView>(R.id.descriptionView)

    fun bind(element: Element, listMode: ListAdapter.ModeType, actionListener: ListItemActionListener<Element>) {
        element.run {
            guestView?.text = guest
            hostView?.text = host
            descriptionView?.text = description
            timeView?.text = dateTime.toString("HH:mm")
        }
        itemView.setOnClickListener {
            actionListener.onListElementClick(element)
        }
        when (listMode) {
            ListAdapter.ModeType.READ_ONLY -> itemView.isEnabled = false
            ListAdapter.ModeType.EDIT_ON_CLICK -> itemView.isEnabled = true
        }
    }
}