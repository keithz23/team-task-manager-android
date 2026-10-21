package com.example.taskmanager.core.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class MessageViewModel : ViewModel() {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val messages: Flow<String> = _messages.asSharedFlow()

    protected fun send(text: String) { _messages.tryEmit(text) }
}
