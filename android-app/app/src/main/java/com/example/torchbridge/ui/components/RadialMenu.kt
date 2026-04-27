package com.example.torchbridge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.example.torchbridge.core.model.ControlAction
import kotlin.math.*

@Composable
fun RadialMenu(
    actions: List<ControlAction>,
    currentAngle: Float,
    onItemSelected: (ControlAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val segmentAngle = 360f / actions.size
    val normalizedAngle = (Math.toDegrees(currentAngle.toDouble()).toFloat() + 450f) % 360f
    val selectedIndex = (normalizedAngle / segmentAngle).toInt() % actions.size

    val painters = actions.map { action ->
        getIconForAction(action)?.let { rememberVectorPainter(it) }
    }

    Box(
        modifier = modifier
            .size(250.dp)
            .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            actions.forEachIndexed { index, action ->
                val startAngle = index * segmentAngle - 90f
                val isSelected = index == selectedIndex

                drawArc(
                    color = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.Transparent,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )

                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    style = Stroke(width = 1.dp.toPx()),
                    size = Size(size.width, size.height)
                )

                val angle = Math.toRadians((startAngle + segmentAngle / 2).toDouble())
                val iconRadius = radius * 0.65f
                val x = center.x + cos(angle).toFloat() * iconRadius
                val y = center.y + sin(angle).toFloat() * iconRadius

                painters[index]?.let { painter ->
                    val iconSize = 32.dp.toPx()
                    translate(x - iconSize / 2, y - iconSize / 2) {
                        with(painter) {
                            draw(Size(iconSize, iconSize))
                        }
                    }
                }
            }
            
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius * 0.3f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }

    // Update selection in parent
    SideEffect {
        onItemSelected(actions[selectedIndex])
    }
}
