package com.example.torchbridge.ui.screens

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.torchbridge.R
import com.example.torchbridge.core.manager.GyroscopeManager
import com.example.torchbridge.core.model.ButtonState
import com.example.torchbridge.core.model.ControlAction
import com.example.torchbridge.core.model.ControlEvent
import com.example.torchbridge.network.socket.SocketClient
import com.example.torchbridge.ui.components.GameButton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ControlScreen() {
    val context = LocalContext.current
    var ip by remember { mutableStateOf("10.144.233.208") }
    val socketClient = remember { SocketClient(ip, 12345) }
    
    var isFreeView by remember { mutableStateOf(false) }
    var isGyroEnabled by remember { mutableStateOf(false) }
    val gyroManager = remember { GyroscopeManager(context, socketClient) }
    
    var showSettings by remember { mutableStateOf(false) }

    // Main Joystick State
    var joystickCenter by remember { mutableStateOf(Offset.Zero) }
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }
    var isDraggingJoystick by remember { mutableStateOf(false) }

    // Eye Joystick State
    var eyeJoystickOffset by remember { mutableStateOf(Offset.Zero) }

    // Initial connection
    LaunchedEffect(Unit) {
        socketClient.connect()
    }

    // Gyro lifecycle
    DisposableEffect(isGyroEnabled) {
        if (isGyroEnabled) gyroManager.start()
        onDispose { gyroManager.stop() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val width = constraints.maxWidth.toFloat()
        val joystickZoneWidth = width * 0.35f
        val aimWidth = width * 0.65f
        val density = LocalDensity.current
        val joystickRadius = with(density) { 60.dp.toPx() }
        val thumbRadius = with(density) { 30.dp.toPx() }

        // 1. DYNAMIC JOYSTICK AREA (LEFT 35%)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { joystickZoneWidth.toDp() })
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            joystickCenter = offset
                            isDraggingJoystick = true
                        },
                        onDrag = { change, dragAmount ->
                            val totalOffset = joystickOffset + dragAmount
                            val distance = sqrt(totalOffset.x * totalOffset.x + totalOffset.y * totalOffset.y)
                            
                            if (distance <= joystickRadius) {
                                joystickOffset = totalOffset
                            } else {
                                val angle = atan2(totalOffset.y, totalOffset.x)
                                joystickOffset = Offset(
                                    x = cos(angle) * joystickRadius,
                                    y = sin(angle) * joystickRadius
                                )
                            }

                            val normalizedX = (joystickOffset.x / joystickRadius).coerceIn(-1f, 1f)
                            val normalizedY = (joystickOffset.y / joystickRadius).coerceIn(-1f, 1f)
                            
                            socketClient.send(
                                ControlEvent(
                                    action = ControlAction.MOVE,
                                    x = normalizedX,
                                    y = normalizedY
                                )
                            )
                        },
                        onDragEnd = {
                            isDraggingJoystick = false
                            joystickOffset = Offset.Zero
                            socketClient.send(ControlEvent(ControlAction.MOVE_STOP))
                        }
                    )
                }
        ) {
            // Visual Joystick
            if (isDraggingJoystick) {
                Box(
                    modifier = Modifier
                        .offset { 
                            IntOffset(
                                (joystickCenter.x - joystickRadius).toInt(),
                                (joystickCenter.y - joystickRadius).toInt()
                            )
                        }
                        .size(with(density) { (joystickRadius * 2).toDp() })
                ) {
                    // Outer Ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.2f),
                            radius = joystickRadius,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    
                    // Thumb
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (joystickRadius + joystickOffset.x - thumbRadius).toInt(),
                                    (joystickRadius + joystickOffset.y - thumbRadius).toInt()
                                )
                            }
                            .size(with(density) { (thumbRadius * 2).toDp() })
                            .background(Color.White.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }
        }

        // 2. AIM AREA (RIGHT 65%)
        val aimSensitivity = if (isFreeView) 2.5f else 1.2f
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { aimWidth.toDp() })
                .align(Alignment.CenterEnd)
                .pointerInput(isFreeView) {
                    detectDragGestures { change, dragAmount ->
                        if (dragAmount.x != 0f || dragAmount.y != 0f) {
                            socketClient.send(
                                ControlEvent(
                                    action = ControlAction.AIM,
                                    dx = dragAmount.x * aimSensitivity,
                                    dy = dragAmount.y * aimSensitivity
                                )
                            )
                        }
                        change.consume()
                    }
                }
        )

        // --- HUD OVERLAYS ---

        // 3. FIRE (TOP LEFT)
        Box(Modifier.align(Alignment.TopStart).padding(start = 40.dp, top = 40.dp)) {
            GameButton(R.drawable.ic_fire, "FIRE", socketClient, 85.dp)
        }

        // 4. EYE (FREE VIEW) - TOP CENTER
        Box(Modifier.align(Alignment.TopCenter).padding(top = 40.dp)) {
            val eyeColor by animateColorAsState(if (isFreeView) Color.Cyan else Color.White, label = "eyeColor")
            val eyeJoyRadius = with(density) { 25.dp.toPx() }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, eyeColor.copy(alpha = 0.3f), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isFreeView = true
                                socketClient.send(ControlEvent(ControlAction.FREE_VIEW, ButtonState.DOWN))
                            },
                            onDrag = { change, dragAmount ->
                                val totalOffset = eyeJoystickOffset + dragAmount
                                val distance = sqrt(totalOffset.x * totalOffset.x + totalOffset.y * totalOffset.y)
                                
                                if (distance <= eyeJoyRadius) {
                                    eyeJoystickOffset = totalOffset
                                } else {
                                    val angle = atan2(totalOffset.y, totalOffset.x)
                                    eyeJoystickOffset = Offset(
                                        x = cos(angle) * eyeJoyRadius,
                                        y = sin(angle) * eyeJoyRadius
                                    )
                                }
                                change.consume()
                            },
                            onDragEnd = {
                                isFreeView = false
                                socketClient.send(ControlEvent(ControlAction.FREE_VIEW, ButtonState.UP))
                                eyeJoystickOffset = Offset.Zero
                            },
                            onDragCancel = {
                                isFreeView = false
                                socketClient.send(ControlEvent(ControlAction.FREE_VIEW, ButtonState.UP))
                                eyeJoystickOffset = Offset.Zero
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Eye Icon (The "Thumb")
                Icon(
                    Icons.Default.Visibility, 
                    "FREE_VIEW", 
                    tint = eyeColor, 
                    modifier = Modifier
                        .size(30.dp)
                        .offset { 
                            IntOffset(eyeJoystickOffset.x.toInt(), eyeJoystickOffset.y.toInt()) 
                        }
                )
            }
        }

        // 5. PEEKS, MAP & SETTINGS (TOP RIGHT)
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameButton(R.drawable.ic_peek_left, "PEEK_LEFT", socketClient, 55.dp)
            GameButton(R.drawable.ic_peek_right, "PEEK_RIGHT", socketClient, 55.dp)
            GameButton(R.drawable.ic_map, "MAP", socketClient, 55.dp)
            
            // Settings Toggle Button
            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(painterResource(R.drawable.ic_settings), "SETTINGS", tint = Color.White, modifier = Modifier.size(25.dp))
            }
        }

        // 6. RIGHT SIDE ACTIONS
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            GameButton(R.drawable.ic_jump, "JUMP", socketClient, 70.dp)
            GameButton(R.drawable.ic_crouch, "CROUCH", socketClient, 70.dp)
            GameButton(R.drawable.ic_scope, "ADS", socketClient, 70.dp)
        }

        // 7. BOTTOM CENTER (WEAPONS, RELOAD)
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameButton(R.drawable.ic_hand, "WEAPON_1", socketClient, 80.dp)
            GameButton(R.drawable.ic_hand, "WEAPON_2", socketClient, 80.dp)
            GameButton(R.drawable.ic_hand, "THROW_GRENADE", socketClient, 60.dp)
            GameButton(R.drawable.ic_reload, "RELOAD", socketClient, 65.dp)
        }

        // 8. MEDKIT (BOTTOM LEFT)
        Box(Modifier.align(Alignment.BottomStart).padding(start = 140.dp, bottom = 40.dp)) {
            GameButton(R.drawable.ic_hand, "CONSUMABLE_MEDKIT", socketClient, 65.dp)
        }

        // Settings Dialog / Overlay
        if (showSettings || !socketClient.isConnected) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(32.dp).widthIn(max = 400.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (!socketClient.isConnected) "Connect to Server" else "Settings",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(24.dp))
                        
                        TextField(
                            value = ip, 
                            onValueChange = { ip = it }, 
                            label = { Text("Server IP Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Enable Gyroscope")
                            Switch(checked = isGyroEnabled, onCheckedChange = { isGyroEnabled = it })
                        }
                        
                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { 
                                // TODO: Implement Layout Customization Mode
                                showSettings = false
                                Log.d("SETTINGS", "Customize Controls Clicked")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Customize Controls")
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (showSettings) {
                                OutlinedButton(onClick = { showSettings = false }, modifier = Modifier.weight(1f)) {
                                    Text("Close")
                                }
                            }
                            Button(
                                onClick = { 
                                    socketClient.connect()
                                    if (socketClient.isConnected) showSettings = false 
                                }, 
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (!socketClient.isConnected) "Connect" else "Reconnect")
                            }
                        }
                    }
                }
            }
        }
    }
}
