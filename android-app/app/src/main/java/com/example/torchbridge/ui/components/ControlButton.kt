package com.example.torchbridge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.torchbridge.core.model.ControlAction

@Composable
fun ControlButton(
    action: ControlAction,
    label: String,
    modifier: Modifier = Modifier,
    onDown: () -> Unit,
    onUp: () -> Unit,
    onTap: () -> Unit = {},
    onHold: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")
    val alpha by animateFloatAsState(if (isPressed) 0.6f else 0.3f, label = "alpha")

    val icon = getIconForAction(action)
    val shape = getShapeForAction(action)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha),
                        Color.Black.copy(alpha = alpha)
                    )
                )
            )
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), shape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    onDown()
                    
                    val startTime = System.currentTimeMillis()
                    var isHoldTriggered = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val currentTime = System.currentTimeMillis()
                        
                        if (currentTime - startTime > 200 && !isHoldTriggered) {
                            onHold()
                            isHoldTriggered = true
                        }

                        if (event.changes.any { it.id == down.id && !it.pressed }) {
                            isPressed = false
                            onUp()
                            if (currentTime - startTime <= 200) {
                                onTap()
                            }
                            break
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.padding(8.dp).fillMaxSize(0.6f)
            )
        } else {
            Text(text = label, color = Color.White, fontSize = 10.sp)
        }
    }
}

fun getIconForAction(action: ControlAction): ImageVector? {
    return when (action) {
        ControlAction.FIRE -> Icons.Default.Adjust
        ControlAction.AIM -> Icons.Default.FilterCenterFocus
        ControlAction.JUMP -> Icons.Default.KeyboardDoubleArrowUp
        ControlAction.CROUCH -> Icons.Default.ExpandMore
        ControlAction.PRONE -> Icons.Default.KeyboardArrowDown
        ControlAction.SPRINT -> Icons.Default.Speed
        ControlAction.BAG -> Icons.Default.ShoppingBag
        ControlAction.WEAPON_1 -> Icons.Default.LooksOne
        ControlAction.WEAPON_2 -> Icons.Default.LooksTwo
        ControlAction.PEEK_LEFT -> Icons.Default.TurnLeft
        ControlAction.PEEK_RIGHT -> Icons.Default.TurnRight
        ControlAction.CONSUMABLE_FIRSTAID -> Icons.Default.MedicalServices
        ControlAction.THROW_GRENADE -> Icons.Default.RadioButtonChecked
        ControlAction.RELOAD -> Icons.Default.Sync
        ControlAction.INTERACT -> Icons.Default.FrontHand
        ControlAction.MAP -> Icons.Default.Map
        ControlAction.SETTINGS -> Icons.Default.Settings
        else -> null
    }
}

fun getShapeForAction(action: ControlAction): Shape {
    return when (action) {
        ControlAction.FIRE -> CircleShape
        ControlAction.AIM -> CircleShape
        ControlAction.RELOAD -> CircleShape
        ControlAction.INTERACT -> RoundedCornerShape(12.dp)
        ControlAction.SPRINT -> RoundedCornerShape(20.dp) // Capsule
        ControlAction.WEAPON_1, ControlAction.WEAPON_2 -> RoundedCornerShape(8.dp)
        else -> RoundedCornerShape(4.dp)
    }
}
