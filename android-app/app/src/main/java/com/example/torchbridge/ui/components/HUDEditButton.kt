package com.example.torchbridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torchbridge.core.model.ControlButtonConfig
import kotlin.math.roundToInt

@Composable
fun HUDEditButton(
    config: ControlButtonConfig,
    onUpdate: (ControlButtonConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset { IntOffset(config.normalizedX.roundToInt(), config.normalizedY.roundToInt()) }
            .size(config.size.dp)
            .background(Color.Red.copy(alpha = 0.4f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onUpdate(config.copy(
                        normalizedX = config.normalizedX + dragAmount.x,
                        normalizedY = config.normalizedY + dragAmount.y
                    ))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(config.label, color = Color.White, fontSize = 10.sp)
            Text("${config.size.toInt()}", color = Color.White, fontSize = 8.sp)
        }
    }
}
