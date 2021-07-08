package com.scorealarm.meeting.rooms.list

interface ListItemActionListener<T> {

    fun onListElementClick(data: T)

}