package com.example.torchbridge.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.torchbridge.core.model.ButtonState
import com.example.torchbridge.core.model.ControlAction
import com.example.torchbridge.core.model.ControlEvent
import com.example.torchbridge.network.socket.SocketClient

@Composable
fun GameButton(icon: Int, action: String, socket: SocketClient, size: Dp = 60.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color.White.copy(alpha = 0.1f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .pointerInput(action, socket) {
                detectTapGestures(
                    onPress = {
                        Log.d("BTN", "$action DOWN")
                        val controlAction = try {
                            ControlAction.valueOf(action)
                        } catch (e: Exception) {
                            null
                        }

                        if (controlAction != null) {
                            socket.send(ControlEvent(controlAction, ButtonState.DOWN))
                        } else {
                            socket.send("""{"action":"$action","state":"DOWN"}""")
                        }

                        tryAwaitRelease()

                        if (controlAction != null) {
                            socket.send(ControlEvent(controlAction, ButtonState.UP))
                        } else {
                            socket.send("""{"action":"$action","state":"UP"}""")
                        }
                        Log.d("BTN", "$action UP")
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = action,
            tint = Color.White,
            modifier = Modifier.size(size * 0.7f) // Increased icon size slightly for better visibility
        )
    }
}
