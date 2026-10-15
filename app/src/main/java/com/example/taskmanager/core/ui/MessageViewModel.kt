package com.example.taskmanager.core.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class MessageViewModel : ViewModel() {
    private val _messages = Channel <String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    protected fun send(text: String) { _messages.trySend(text) }
}
