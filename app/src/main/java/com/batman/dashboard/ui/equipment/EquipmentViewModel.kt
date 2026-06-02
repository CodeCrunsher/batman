package com.batman.dashboard.ui.equipment

import androidx.lifecycle.*
import com.batman.dashboard.data.api.NetworkModule
import com.batman.dashboard.data.db.EquipmentDao
import com.batman.dashboard.data.db.EquipmentEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class EquipmentUiState(
    val items: List<EquipmentEntity> = emptyList(),
    val selectedItem: EquipmentEntity? = null,
    val uplinkError: String? = null
)

class EquipmentViewModel(private val dao: EquipmentDao) : ViewModel() {

    private val _uplinkError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<EquipmentUiState> =
        combine(dao.getAllEquipment(), _uplinkError) { items, error ->
            EquipmentUiState(items = items, uplinkError = error)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EquipmentUiState())

    init {
        // Seed from server only when local equipment DB is empty
        viewModelScope.launch {
            dao.getAllEquipment().first().let { items ->
                if (items.isEmpty()) syncWithServer()
            }
        }
    }

    // ── Server sync ──────────────────────────────────────────────────────────

    /**
     * Seeds local Room DB from the Batcomputer backend when the equipment
     * table is empty.  IOException / HttpException are caught so the app never
     * crashes — uplinkError drives the warning banner in the UI.
     */
    fun syncWithServer() {
        viewModelScope.launch {
            try {
                val remote = NetworkModule.api.getEquipment()
                remote.forEach { r ->
                    // EquipmentEntity uses String PKs — convert the server int ID
                    dao.insertOrUpdateEquipment(
                        EquipmentEntity(
                            id = "server_${r.id}",
                            name = r.name,
                            status = r.status,
                            batteryLevel = r.integrityLevel,
                            lastUsed = null,
                            isEnabled = r.status == "OPERATIONAL" || r.status == "ACTIVE" || r.status == "READY",
                            iconKey = iconKeyFor(r.name)
                        )
                    )
                }
                _uplinkError.value = null
            } catch (e: IOException) {
                _uplinkError.value = "Uplink offline. Batcomputer awakening server..."
            } catch (e: HttpException) {
                _uplinkError.value = "Server error — HTTP ${e.code()}. Using local inventory."
            }
        }
    }

    fun clearUplinkError() { _uplinkError.value = null }

    // ── Local mutations ───────────────────────────────────────────────────────

    fun toggleEquipment(item: EquipmentEntity) {
        viewModelScope.launch {
            val newEnabled = !item.isEnabled
            val newStatus = if (newEnabled) "ACTIVE" else "STANDBY"
            dao.updateEquipmentStatus(item.id, newEnabled, newStatus)
        }
    }

    fun drainBattery(item: EquipmentEntity, amount: Int) {
        viewModelScope.launch {
            val newLevel = (item.batteryLevel - amount).coerceAtLeast(0)
            dao.updateBatteryLevel(item.id, newLevel)
        }
    }

    fun chargeBattery(item: EquipmentEntity) {
        viewModelScope.launch {
            dao.updateBatteryLevel(item.id, 100)
            dao.updateEquipmentStatus(item.id, false, "CHARGING")
        }
    }

    companion object {
        fun factory(dao: EquipmentDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    EquipmentViewModel(dao) as T
            }

        fun iconKeyFor(name: String): String = when {
            name.contains("suit", ignoreCase = true) ||
            name.contains("armor", ignoreCase = true)    -> "suit"
            name.contains("mobile", ignoreCase = true)   -> "car"
            name.contains("wing", ignoreCase = true) ||
            name.contains("plane", ignoreCase = true)    -> "plane"
            name.contains("batarang", ignoreCase = true) -> "batarang"
            name.contains("grapple", ignoreCase = true)  -> "grapple"
            name.contains("goggle", ignoreCase = true)   -> "goggles"
            name.contains("emp", ignoreCase = true)      -> "emp"
            name.contains("comm", ignoreCase = true)     -> "comm"
            else                                         -> "suit"
        }
    }
}
