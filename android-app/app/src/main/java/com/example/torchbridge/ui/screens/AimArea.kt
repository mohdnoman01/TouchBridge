package com.example.torchbridge.ui.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.torchbridge.core.model.ControlAction
import com.example.torchbridge.core.model.ControlEvent
import com.example.torchbridge.network.socket.SocketClient

@Composable
fun AimArea(socketClient: SocketClient, modifier: Modifier = Modifier) {
    var lastSentTime by remember { mutableLongStateOf(0L) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.65f)
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    val now = System.currentTimeMillis()
                    if (now - lastSentTime > 16) {
                        socketClient.send(ControlEvent(ControlAction.AIM, dx = drag.x, dy = drag.y))
                        lastSentTime = now
                    }
                    change.consume()
                }
            }
    )
}
