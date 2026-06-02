package com.batman.dashboard.ui.emergency

import androidx.lifecycle.*
import com.batman.dashboard.data.db.EmergencyContactEntity
import com.batman.dashboard.data.db.EmergencyDao
import com.batman.dashboard.data.db.EmergencyLogEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EmergencyUiState(
    val contacts: List<EmergencyContactEntity> = emptyList(),
    val alertLog: List<EmergencyLogEntity> = emptyList(),
    val isSosActive: Boolean = false,
    val sosCountdown: Int = 0,
    val selectedAlertType: String = "TACTICAL SUPPORT",
    val lastAlertSentAt: Long? = null,
    val showSuccess: Boolean = false
)

class EmergencyViewModel(private val dao: EmergencyDao) : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    private var sosJob: Job? = null

    init {
        viewModelScope.launch {
            launch { dao.getActiveContacts().collect { contacts -> _uiState.update { s -> s.copy(contacts = contacts) } } }
            launch { dao.getRecentAlerts().collect { logs -> _uiState.update { s -> s.copy(alertLog = logs) } } }
        }
    }

    fun selectAlertType(type: String) = _uiState.update { it.copy(selectedAlertType = type) }

    fun triggerSOS() {
        _uiState.update { it.copy(isSosActive = true, sosCountdown = 5, showSuccess = false) }
        sosJob = viewModelScope.launch {
            for (i in 4 downTo 0) {
                delay(1000L)
                _uiState.update { it.copy(sosCountdown = i) }
            }
            sendEmergencyAlert()
        }
    }

    fun cancelSOS() {
        sosJob?.cancel()
        _uiState.update { it.copy(isSosActive = false, sosCountdown = 0) }
    }

    private suspend fun sendEmergencyAlert() {
        val type = _uiState.value.selectedAlertType
        dao.logEmergency(EmergencyLogEntity(alertType = type, resolvedAt = null))
        _uiState.update { it.copy(isSosActive = false, sosCountdown = 0, lastAlertSentAt = System.currentTimeMillis(), showSuccess = true) }
        delay(3000L)
        _uiState.update { it.copy(showSuccess = false) }
    }

    companion object {
        fun factory(dao: EmergencyDao): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = EmergencyViewModel(dao) as T
        }
    }
}
