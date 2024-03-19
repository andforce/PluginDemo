package com.andforce.socket

sealed class MouseEvent(open val x:Int, open val y:Int, open val remoteWidth: Int, open val remoteHeight: Int) {
    data class Down(override val x:Int, override val y:Int, override val remoteWidth: Int, override val remoteHeight: Int) : MouseEvent(x, y, remoteWidth, remoteHeight)
    data class Up(override val x:Int, override val y:Int, override val remoteWidth: Int, override val remoteHeight: Int) : MouseEvent(x, y, remoteWidth, remoteHeight)
    data class Move(override val x:Int, override val y:Int, override val remoteWidth: Int, override val remoteHeight: Int) : MouseEvent(x, y, remoteWidth, remoteHeight)
}
