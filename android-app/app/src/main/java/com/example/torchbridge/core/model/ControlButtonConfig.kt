package com.example.torchbridge.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ControlButtonConfig(
    val id: String,
    val action: ControlAction,
    val label: String,
    var normalizedX: Float = 0f,
    var normalizedY: Float = 0f,
    var size: Float = 70f,
    var opacity: Float = 0.5f,
    var icon: Int? = null
)
