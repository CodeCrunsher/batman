package com.batman.dashboard.ui.map

import androidx.lifecycle.*
import com.batman.dashboard.data.db.CrimePinDao
import com.batman.dashboard.data.db.CrimePinEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MapUiState(
    val activePins: List<CrimePinEntity> = emptyList(),
    val selectedPin: CrimePinEntity? = null,
    val isAddPinDialogOpen: Boolean = false,
    val showAllPins: Boolean = true
)

class MapViewModel(private val dao: CrimePinDao) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getActiveCrimePins().collect { pins ->
                _uiState.update { it.copy(activePins = pins) }
            }
        }
    }

    fun selectPin(pin: CrimePinEntity?) = _uiState.update { it.copy(selectedPin = pin) }
    fun openAddDialog() = _uiState.update { it.copy(isAddPinDialogOpen = true) }
    fun closeAddDialog() = _uiState.update { it.copy(isAddPinDialogOpen = false) }

    fun addCrimePin(type: String, district: String, description: String, threatLevel: Int, x: Float, y: Float) {
        viewModelScope.launch {
            dao.insertCrimePin(
                CrimePinEntity(type = type, district = district, description = description,
                    threatLevel = threatLevel, mapX = x, mapY = y)
            )
            closeAddDialog()
        }
    }

    fun resolvePin(pin: CrimePinEntity) {
        viewModelScope.launch { dao.deactivateCrimePin(pin.id) }
        _uiState.update { it.copy(selectedPin = null) }
    }

    companion object {
        fun factory(dao: CrimePinDao): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MapViewModel(dao) as T
        }
    }
}
