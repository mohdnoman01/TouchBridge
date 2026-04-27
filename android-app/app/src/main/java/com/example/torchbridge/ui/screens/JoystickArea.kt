package com.example.torchbridge.ui.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.example.torchbridge.core.model.ButtonState
import com.example.torchbridge.core.model.ControlAction
import com.example.torchbridge.core.model.ControlEvent
import com.example.torchbridge.network.socket.SocketClient

@Composable
fun JoystickArea(socketClient: SocketClient, modifier: Modifier = Modifier) {
    var center by remember { mutableStateOf(Offset.Zero) }
    var activeDirections by remember { mutableStateOf(setOf<ControlAction>()) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.35f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        center = offset
                    },
                    onDragEnd = {
                        activeDirections.forEach {
                            socketClient.send(ControlEvent(it, ButtonState.UP))
                        }
                        activeDirections = emptySet()
                    },
                    onDrag = { change, _ ->
                        val dx = change.position.x - center.x
                        val dy = change.position.y - center.y
                        val threshold = 30f

                        val newDirections = mutableSetOf<ControlAction>()
                        if (dy < -threshold) newDirections.add(ControlAction.MOVE_FORWARD)
                        if (dy > threshold) newDirections.add(ControlAction.MOVE_BACK)
                        if (dx < -threshold) newDirections.add(ControlAction.MOVE_LEFT)
                        if (dx > threshold) newDirections.add(ControlAction.MOVE_RIGHT)

                        newDirections.forEach {
                            if (!activeDirections.contains(it)) {
                                socketClient.send(ControlEvent(it, ButtonState.DOWN))
                            }
                        }

                        activeDirections.forEach {
                            if (!newDirections.contains(it)) {
                                socketClient.send(ControlEvent(it, ButtonState.UP))
                            }
                        }

                        activeDirections = newDirections
                    }
                )
            }
    )
}
