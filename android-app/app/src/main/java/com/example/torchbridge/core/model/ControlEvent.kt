package com.example.torchbridge.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ControlAction {
    MOVE_FORWARD, MOVE_BACK, MOVE_LEFT, MOVE_RIGHT,
    FIRE, AIM, ADS, JUMP, CROUCH, PRONE, SPRINT, AUTO_SPRINT,
    PEEK_LEFT, PEEK_RIGHT, BAG, WEAPON_1, WEAPON_2, FPP_TPP,
    RELOAD, INTERACT, MAP, SETTINGS, FREE_VIEW,
    // Movement
    MOVE, MOVE_STOP,
    // Phase 3: Consumables
    CONSUMABLE_BANDAGE, CONSUMABLE_FIRSTAID, CONSUMABLE_MEDKIT,
    CONSUMABLE_DRINK, CONSUMABLE_PAINKILLER,
    // Phase 3: Throwables
    THROW_GRENADE, THROW_SMOKE, THROW_STUN
}

@Serializable
enum class ButtonState {
    DOWN, UP, TAP
}

@Serializable
data class ControlEvent(
    val action: ControlAction,
    val state: ButtonState? = null,
    val dx: Float? = null,
    val dy: Float? = null,
    val x: Float? = null,
    val y: Float? = null
)
