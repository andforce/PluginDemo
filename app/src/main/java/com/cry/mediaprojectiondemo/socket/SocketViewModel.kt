package com.cry.mediaprojectiondemo.socket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andforce.socket.MouseEvent
import com.andforce.socket.SocketClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch

class SocketViewModel : ViewModel() {
    private val socketRepository = SocketRepository()

    private val _eventFlow = MutableSharedFlow<MouseEvent?>(replay = 0)
    var eventFlow: SharedFlow<MouseEvent?> = _eventFlow

    fun listenEvent(socketClient: SocketClient) {
        viewModelScope.launch {
            socketRepository.listenEvent(socketClient).buffer(1024).collect {
                _eventFlow.emit(it)
            }
        }
    }
}