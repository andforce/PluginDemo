package com.cry.mediaprojectiondemo.socket

import com.andforce.socket.MouseEvent
import com.andforce.socket.MouseEventListener
import com.andforce.socket.SocketClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class SocketRepository {

    suspend fun listenEvent(socketClient: SocketClient): Flow<MouseEvent> = callbackFlow {
        val listener = object : MouseEventListener {
            override fun onDown(mouseEvent: MouseEvent) {
                trySend(mouseEvent)
            }

            override fun onUp(mouseEvent: MouseEvent) {
                trySend(mouseEvent)
            }

            override fun onMove(mouseEvent: MouseEvent) {
                trySend(mouseEvent)
            }
        }
        socketClient.registerMouseEventListener(listener)
        awaitClose {
            socketClient.unRegisterMouseEventListener()
        }
    }

}