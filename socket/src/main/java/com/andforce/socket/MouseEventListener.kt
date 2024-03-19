package com.andforce.socket

interface MouseEventListener {

    fun onDown(mouseEvent: MouseEvent)
    fun onUp(mouseEvent: MouseEvent)
    fun onMove(mouseEvent: MouseEvent)
}