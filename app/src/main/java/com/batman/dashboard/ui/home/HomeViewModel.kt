package com.batman.dashboard.ui.home

import androidx.lifecycle.*
import com.batman.dashboard.data.db.CrimePinDao
import com.batman.dashboard.data.db.MissionDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val activeMissionCount: Int = 0,
    val activeCrimeCount: Int = 0,
    val threatLevel: Float = 0f,
    val currentTime: String = "",
    val dateString: String = ""
)

class HomeViewModel(
    private val missionDao: MissionDao,
    private val crimePinDao: CrimePinDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                missionDao.getActiveMissionCount(),
                crimePinDao.getActivePinCount()
            ) { missions, crimes ->
                val threat = (crimes.coerceAtMost(10) / 10f * 0.6f + missions.coerceAtMost(10) / 10f * 0.4f).coerceIn(0f, 1f)
                HomeUiState(
                    activeMissionCount = missions,
                    activeCrimeCount = crimes,
                    threatLevel = threat
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    companion object {
        fun factory(missionDao: MissionDao, crimePinDao: CrimePinDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(missionDao, crimePinDao) as T
            }
    }
}
