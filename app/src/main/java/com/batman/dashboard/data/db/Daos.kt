package com.batman.dashboard.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions ORDER BY priority ASC, dueDate ASC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE status != 'COMPLETED' ORDER BY priority ASC")
    fun getActiveMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Delete
    suspend fun deleteMission(mission: MissionEntity)

    @Query("SELECT COUNT(*) FROM missions WHERE status != 'COMPLETED'")
    fun getActiveMissionCount(): Flow<Int>
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE allyId = :allyId ORDER BY timestamp ASC")
    fun getMessagesForAlly(allyId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessage(): Flow<MessageEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE allyId = :allyId")
    suspend fun clearConversation(allyId: String)
}

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment ORDER BY name ASC")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE id = :id")
    fun getEquipmentById(id: String): Flow<EquipmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEquipment(equipment: EquipmentEntity)

    @Query("UPDATE equipment SET isEnabled = :isEnabled, status = :status WHERE id = :id")
    suspend fun updateEquipmentStatus(id: String, isEnabled: Boolean, status: String)

    @Query("UPDATE equipment SET batteryLevel = :level WHERE id = :id")
    suspend fun updateBatteryLevel(id: String, level: Int)
}

@Dao
interface CrimePinDao {
    @Query("SELECT * FROM crime_pins WHERE isActive = 1 ORDER BY reportedAt DESC")
    fun getActiveCrimePins(): Flow<List<CrimePinEntity>>

    @Query("SELECT * FROM crime_pins ORDER BY reportedAt DESC")
    fun getAllCrimePins(): Flow<List<CrimePinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrimePin(pin: CrimePinEntity): Long

    @Query("UPDATE crime_pins SET isActive = 0 WHERE id = :id")
    suspend fun deactivateCrimePin(id: Long)

    @Delete
    suspend fun deleteCrimePin(pin: CrimePinEntity)

    @Query("SELECT COUNT(*) FROM crime_pins WHERE isActive = 1")
    fun getActivePinCount(): Flow<Int>
}

@Dao
interface EmergencyDao {
    @Query("SELECT * FROM emergency_contacts WHERE isActive = 1")
    fun getActiveContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity): Long

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)

    @Query("SELECT * FROM emergency_log ORDER BY triggeredAt DESC LIMIT 20")
    fun getRecentAlerts(): Flow<List<EmergencyLogEntity>>

    @Insert
    suspend fun logEmergency(log: EmergencyLogEntity): Long
}
