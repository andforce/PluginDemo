package com.andforce.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

class SocketClient(url: String) {
    private var socket: Socket? = null

    private var mouseEventListener: MouseEventListener? = null

    fun registerMouseEventListener(listener: MouseEventListener) {
        mouseEventListener = listener
    }

    fun unRegisterMouseEventListener() {
        mouseEventListener = null
    }

    init {
        try {
            socket = IO.socket(url)
        } catch (e: Exception) {
            Log.e("SocketClient", e.toString())
        }
    }

    fun startConnection() {
        socket?.on(Socket.EVENT_CONNECT, Emitter.Listener {
            Log.d("SocketClient", "connect")
        })

        socket?.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            Log.d("SocketClient", "disconnect")
        })

        socket?.on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener {
            Log.d("SocketClient", "connect error")
        })

        socket?.on("event", Emitter.Listener { args ->
            Log.d("SocketClient", args[0].toString())
        })

        socket?.on("mouse-down", Emitter.Listener { args ->
            val data = args[0] as JSONObject
            val down = MouseEvent.Down(data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
            Log.d("SocketClient", "mousedown" + args[0].toString())
            mouseEventListener?.onDown(down)
        })

        socket?.on("mouse-up", Emitter.Listener { args ->
            Log.d("SocketClient", "mouseup" + args[0].toString())
            val data = args[0] as JSONObject
            val down = MouseEvent.Up(data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
            mouseEventListener?.onUp(down)
        })

        socket?.on("mouse-move", Emitter.Listener { args ->
            Log.d("SocketClient", "mousemove" + args[0].toString())
            val data = args[0] as JSONObject
            val down = MouseEvent.Move(data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
            mouseEventListener?.onMove(down)
        })
        // mouse-click
//        socket?.on("mouse-click", Emitter.Listener { args ->
//            Log.d("SocketClient", "mouse-click" + args[0].toString())
//            val data = args[0] as JSONObject
//            val down = MouseEvent.Click(data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
//            mouseEventListener?.onClick(down)
//        })
        socket?.connect()
    }
    fun send(bitmapArray: ByteArray) {
        socket?.emit("image", bitmapArray)
    }

    fun release() {

        socket?.off(Socket.EVENT_CONNECT)
        socket?.off(Socket.EVENT_CONNECT_ERROR)
        socket?.off(Socket.EVENT_DISCONNECT)
        socket?.off("event")

        socket?.disconnect()
        socket?.close()
    }
}