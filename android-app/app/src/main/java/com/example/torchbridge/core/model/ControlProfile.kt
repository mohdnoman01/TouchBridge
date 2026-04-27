package com.example.torchbridge.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ControlProfile(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val buttons: List<ControlButtonConfig>
)

@Serializable
data class ProfilesData(
    val activeProfileId: String,
    val profiles: List<ControlProfile>
)
