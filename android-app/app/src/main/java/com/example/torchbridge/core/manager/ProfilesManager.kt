package com.example.torchbridge.core.manager

import android.content.Context
import com.example.torchbridge.core.model.ControlButtonConfig
import com.example.torchbridge.core.model.ControlProfile
import com.example.torchbridge.core.model.ProfilesData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class ProfilesManager(private val context: Context) {
    private val file = File(context.filesDir, "profiles.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun loadData(): ProfilesData {
        return if (file.exists()) {
            try {
                json.decodeFromString(file.readText())
            } catch (e: Exception) {
                createDefaultData()
            }
        } else {
            createDefaultData()
        }
    }

    private fun createDefaultData(): ProfilesData {
        val defaultProfile = ControlProfile(name = "Default", buttons = emptyList())
        val data = ProfilesData(activeProfileId = defaultProfile.id, profiles = listOf(defaultProfile))
        saveData(data)
        return data
    }

    fun saveData(data: ProfilesData) {
        file.writeText(json.encodeToString(data))
    }

    fun createProfile(name: String, baseButtons: List<ControlButtonConfig>): ControlProfile {
        val newProfile = ControlProfile(name = name, buttons = baseButtons.map { it.copy() })
        val currentData = loadData()
        val newData = currentData.copy(profiles = currentData.profiles + newProfile)
        saveData(newData)
        return newProfile
    }

    fun deleteProfile(id: String) {
        val currentData = loadData()
        if (currentData.profiles.size <= 1) return // Prevent deleting last profile
        
        val newProfiles = currentData.profiles.filter { it.id != id }
        val newActiveId = if (currentData.activeProfileId == id) newProfiles.first().id else currentData.activeProfileId
        saveData(ProfilesData(newActiveId, newProfiles))
    }
    
    fun renameProfile(id: String, newName: String) {
        val currentData = loadData()
        val newProfiles = currentData.profiles.map { 
            if (it.id == id) it.copy(name = newName) else it
        }
        saveData(currentData.copy(profiles = newProfiles))
    }
}
