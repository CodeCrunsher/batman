package com.batman.dashboard.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// ───────────── MISSIONS ─────────────
@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val priority: String,       // CRITICAL, HIGH, MEDIUM, LOW
    val status: String,         // PENDING, IN_PROGRESS, COMPLETED
    val category: String,       // RECON, COMBAT, INFILTRATION, INVESTIGATION
    val dueDate: Long?,         // epoch millis, nullable
    val createdAt: Long = System.currentTimeMillis()
)

// ───────────── MESSAGES ─────────────
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val allyId: String,         // e.g. "alfred", "robin"
    val content: String,
    val isFromBatman: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// ───────────── EQUIPMENT ─────────────
@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey val id: String, // e.g. "batsuit"
    val name: String,
    val status: String,         // ACTIVE, STANDBY, OFFLINE, CHARGING
    val batteryLevel: Int,      // 0-100
    val lastUsed: Long?,
    val isEnabled: Boolean = false,
    val iconKey: String = ""
)

// ───────────── CRIME PINS ─────────────
@Entity(tableName = "crime_pins")
data class CrimePinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,           // ROBBERY, ASSAULT, TERRORISM, DRUG_TRAFFICKING, KIDNAPPING
    val district: String,
    val description: String,
    val threatLevel: Int,       // 1-5
    val reportedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val mapX: Float,            // normalized 0-1 position on map
    val mapY: Float
)

// ───────────── EMERGENCY CONTACTS ─────────────
@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,
    val phone: String,
    val isActive: Boolean = true
)

// ───────────── EMERGENCY LOG ─────────────
@Entity(tableName = "emergency_log")
data class EmergencyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertType: String,
    val resolvedAt: Long?,
    val triggeredAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
