package com.superology.guestorganizer.fragments

import com.superology.guestorganizer.data.models.Element

interface DialogActionListener {

    fun add(element: Element)

    fun update(element: Element)
}