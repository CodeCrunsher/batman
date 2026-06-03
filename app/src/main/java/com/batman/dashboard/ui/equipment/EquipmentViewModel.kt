package com.batman.dashboard.ui.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batman.dashboard.data.api.NetworkModule
import com.batman.dashboard.data.db.EquipmentDao
import com.batman.dashboard.data.db.EquipmentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class EquipmentUiState(
    val items: List<EquipmentEntity> = emptyList(),
    val selectedItem: EquipmentEntity? = null,
    val networkError: String? = null,
)

class EquipmentViewModel(private val dao: EquipmentDao) : ViewModel() {

    private val _networkError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<EquipmentUiState> =
        combine(dao.getAllEquipment(), _networkError) { items, error ->
            EquipmentUiState(items = items, networkError = error)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EquipmentUiState())

    init {
        viewModelScope.launch {
            if (dao.getAllEquipment().first().isEmpty()) syncWithServer()
        }
    }

    fun syncWithServer() {
        viewModelScope.launch {
            try {
                val remote = NetworkModule.api.getEquipment()
                remote.forEach { r ->
                    dao.insertOrUpdateEquipment(
                        EquipmentEntity(
                            id           = "server_${r.id}",
                            name         = r.name,
                            status       = r.status,
                            batteryLevel = r.integrityLevel,
                            lastUsed     = null,
                            isEnabled    = r.status in listOf("OPERATIONAL", "ACTIVE", "READY"),
                            iconKey      = iconKeyFor(r.name),
                        )
                    )
                }
                _networkError.value = null
            } catch (e: IOException) {
                _networkError.value = "Connection error. Retrying..."
            } catch (e: HttpException) {
                _networkError.value = "Server error (${e.code()}). Using local data."
            }
        }
    }

    fun clearNetworkError() { _networkError.value = null }

    fun toggleEquipment(item: EquipmentEntity) {
        viewModelScope.launch {
            val enabled = !item.isEnabled
            dao.updateEquipmentStatus(item.id, enabled, if (enabled) "ACTIVE" else "STANDBY")
        }
    }

    fun drainBattery(item: EquipmentEntity, amount: Int) {
        viewModelScope.launch {
            dao.updateBatteryLevel(item.id, (item.batteryLevel - amount).coerceAtLeast(0))
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
            name.contains("suit",     ignoreCase = true) ||
            name.contains("armor",    ignoreCase = true) -> "suit"
            name.contains("mobile",   ignoreCase = true) -> "car"
            name.contains("wing",     ignoreCase = true) ||
            name.contains("plane",    ignoreCase = true) -> "plane"
            name.contains("batarang", ignoreCase = true) -> "batarang"
            name.contains("grapple",  ignoreCase = true) -> "grapple"
            name.contains("goggle",   ignoreCase = true) -> "goggles"
            name.contains("emp",      ignoreCase = true) -> "emp"
            name.contains("comm",     ignoreCase = true) -> "comm"
            else                                         -> "suit"
        }
    }
}
