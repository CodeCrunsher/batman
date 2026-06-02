package com.batman.dashboard

import android.app.Application
import com.batman.dashboard.data.db.AppDatabase

/**
 * Application-level DI container.
 * All DAOs are exposed here so ViewModelFactory instances can receive them
 * without any framework-based DI (Hilt/Koin).
 * This pattern ensures ViewModels are testable by allowing DAOs to be
 * injected/mocked at the factory level.
 */
class BatmanApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val container: AppContainer by lazy {
        AppContainer(database)
    }
}

/**
 * A simple manual dependency container that exposes all DAOs.
 * ViewModelFactory classes receive exactly the DAOs they need from here,
 * preventing over-injection and memory leaks.
 */
class AppContainer(private val db: AppDatabase) {
    val missionDao    get() = db.missionDao()
    val messageDao    get() = db.messageDao()
    val equipmentDao  get() = db.equipmentDao()
    val crimePinDao   get() = db.crimePinDao()
    val emergencyDao  get() = db.emergencyDao()
}
