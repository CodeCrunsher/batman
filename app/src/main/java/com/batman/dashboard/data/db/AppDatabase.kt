package com.batman.dashboard.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MissionEntity::class,
        MessageEntity::class,
        EquipmentEntity::class,
        CrimePinEntity::class,
        EmergencyContactEntity::class,
        EmergencyLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun missionDao(): MissionDao
    abstract fun messageDao(): MessageDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun crimePinDao(): CrimePinDao
    abstract fun emergencyDao(): EmergencyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "batman_dashboard.db",
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDefaults(database)
                            }
                        }
                    }
                })
                .build()
                .also { INSTANCE = it }
            }
        }

        private suspend fun populateDefaults(db: AppDatabase) {
            val equipment = listOf(
                EquipmentEntity("batsuit",   "Batsuit Mark VII",       "ACTIVE",  92,  null, true,  "suit"),
                EquipmentEntity("batmobile", "Batmobile",              "STANDBY", 78,  null, false, "car"),
                EquipmentEntity("batwing",   "Batwing",                "STANDBY", 65,  null, false, "plane"),
                EquipmentEntity("batarang",  "Batarang Arsenal",       "ACTIVE",  100, null, true,  "batarang"),
                EquipmentEntity("grapple",   "Grapple Gun",            "ACTIVE",  88,  null, true,  "grapple"),
                EquipmentEntity("goggles",   "Detective Mode Goggles", "ACTIVE",  74,  null, true,  "goggles"),
                EquipmentEntity("emp",       "EMP Device",             "OFFLINE", 12,  null, false, "emp"),
                EquipmentEntity("comm",      "Encrypted Comm Unit",    "ACTIVE",  95,  null, true,  "comm"),
            )
            equipment.forEach { db.equipmentDao().insertOrUpdateEquipment(it) }

            val contacts = listOf(
                EmergencyContactEntity(name = "Alfred Pennyworth",   role = "Butler / Field Support", phone = "+1-555-0001"),
                EmergencyContactEntity(name = "Commissioner Gordon", role = "GCPD",                   phone = "+1-555-0002"),
                EmergencyContactEntity(name = "Oracle (Barbara)",    role = "Intelligence",            phone = "+1-555-0003"),
                EmergencyContactEntity(name = "Lucius Fox",          role = "Wayne Tech Support",     phone = "+1-555-0004"),
            )
            contacts.forEach { db.emergencyDao().insertContact(it) }

            val now = System.currentTimeMillis()
            listOf(
                MessageEntity(allyId = "alfred", content = "Good evening. All systems are operational.",  isFromBatman = false, timestamp = now - 3_600_000),
                MessageEntity(allyId = "alfred", content = "The Batmobile has been serviced. Ready.",     isFromBatman = false, timestamp = now - 1_800_000),
            ).forEach { db.messageDao().insertMessage(it) }

            listOf(
                CrimePinEntity(type = "TERRORISM",        district = "The Narrows",      description = "Scarecrow spotted distributing fear toxin near Arkham Bridge.", threatLevel = 5, mapX = 0.28f, mapY = 0.62f),
                CrimePinEntity(type = "ROBBERY",          district = "Diamond District", description = "Armed heist at Gotham First National Bank.",                   threatLevel = 3, mapX = 0.55f, mapY = 0.35f),
                CrimePinEntity(type = "ASSAULT",          district = "Crime Alley",      description = "Gang fight — multiple injuries reported.",                     threatLevel = 2, mapX = 0.38f, mapY = 0.50f),
                CrimePinEntity(type = "KIDNAPPING",       district = "Amusement Mile",   description = "Joker's crew seen with hostages near Ace Chemicals.",          threatLevel = 5, mapX = 0.70f, mapY = 0.42f),
                CrimePinEntity(type = "DRUG_TRAFFICKING", district = "The Bowery",       description = "Venom shipment intercepted — more expected.",                  threatLevel = 4, mapX = 0.45f, mapY = 0.72f),
                CrimePinEntity(type = "ROBBERY",          district = "Old Gotham",       description = "Museum break-in — artifacts stolen.",                          threatLevel = 2, mapX = 0.35f, mapY = 0.28f),
            ).forEach { db.crimePinDao().insertCrimePin(it) }

            listOf(
                MissionEntity(title = "Neutralize Scarecrow",    description = "Track down Dr. Crane and destroy the fear toxin supply in The Narrows.",                    priority = "CRITICAL", status = "IN_PROGRESS", category = "COMBAT",         dueDate = now + 86_400_000),
                MissionEntity(title = "Joker Hostage Rescue",    description = "Locate and rescue hostages held at Amusement Mile. Non-lethal force only.",                 priority = "CRITICAL", status = "PENDING",      category = "INFILTRATION",   dueDate = now + 43_200_000),
                MissionEntity(title = "Venom Lab Raid",          description = "Infiltrate the Bowery warehouse and destroy Bane's Venom production lab.",                  priority = "HIGH",     status = "PENDING",      category = "COMBAT",         dueDate = now + 172_800_000),
                MissionEntity(title = "Bank Heist Investigation", description = "Review security footage and identify the Diamond District bank heist perpetrators.",        priority = "MEDIUM",   status = "PENDING",      category = "INVESTIGATION",  dueDate = now + 259_200_000),
                MissionEntity(title = "Arkham Patrol",           description = "Routine patrol around Arkham Island perimeter.",                                            priority = "LOW",      status = "PENDING",      category = "RECON",          dueDate = now + 432_000_000),
            ).forEach { db.missionDao().insertMission(it) }
        }
    }
}
