package com.cry.mediaprojectiondemo.socket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andforce.socket.MouseEvent
import com.andforce.socket.SocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SocketViewModel : ViewModel() {
    private val socketRepository = SocketRepository()

    private val _eventFlow = MutableStateFlow<MouseEvent?>(null)
    var eventFlow: StateFlow<MouseEvent?> = _eventFlow

    fun listenEvent(socketClient: SocketClient) {
        viewModelScope.launch {
            socketRepository.listenEvent(socketClient).collect {
                _eventFlow.value = it
            }
        }
    }
}