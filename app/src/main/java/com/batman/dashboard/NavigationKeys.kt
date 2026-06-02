package com.batman.dashboard

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable object HomeKey : NavKey
@Serializable object MissionsKey : NavKey
@Serializable object CommsKey : NavKey
@Serializable data class ChatKey(val allyId: String, val allyName: String) : NavKey
@Serializable object EquipmentKey : NavKey
@Serializable object MapKey : NavKey
@Serializable object MusicKey : NavKey
@Serializable object WayneKey : NavKey
@Serializable object EmergencyKey : NavKey
