package com.superology.guestorganizer.enums

enum class NotificationType(private val type: String) {
    GUEST_INCOMING("guestIncoming"),
    DATA_CHANGE("dataChange");

    fun getId() = type
}