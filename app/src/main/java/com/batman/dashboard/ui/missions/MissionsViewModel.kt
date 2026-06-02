package com.batman.dashboard.ui.missions

import androidx.lifecycle.*
import com.batman.dashboard.data.api.NetworkModule
import com.batman.dashboard.data.db.MissionDao
import com.batman.dashboard.data.db.MissionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class MissionsUiState(
    val missions: List<MissionEntity> = emptyList(),
    val filterStatus: String = "ALL",
    val filterPriority: String = "ALL",
    val filterCategory: String = "ALL",
    val isAddDialogOpen: Boolean = false,
    val editingMission: MissionEntity? = null,
    val uplinkError: String? = null
)

class MissionsViewModel(private val dao: MissionDao) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionsUiState())
    val uiState: StateFlow<MissionsUiState> = _uiState.asStateFlow()

    private val _allMissions = MutableStateFlow<List<MissionEntity>>(emptyList())

    init {
        viewModelScope.launch {
            dao.getAllMissions().collect { missions ->
                _allMissions.value = missions
                applyFilters()
                // Seed from server only when local DB is empty on first launch
                if (missions.isEmpty()) syncWithServer()
            }
        }
    }

    // ── Server sync (seed if empty) ───────────────────────────────────────────
    fun syncWithServer() {
        viewModelScope.launch {
            try {
                val remote = NetworkModule.api.getMissions()
                remote.forEach { r ->
                    dao.insertMission(
                        MissionEntity(
                            // id = 0 triggers autoGenerate; server IDs are separate
                            title = r.title,
                            description = r.description ?: "",
                            priority = when (r.priority) {
                                1    -> "HIGH"
                                3, 4 -> "LOW"
                                else -> "MEDIUM"
                            },
                            status = if (r.isCompleted == 1) "COMPLETED" else "PENDING",
                            category = "CRIME",
                            dueDate = null   // server data carries no due-date timestamps
                        )
                    )
                }
                _uiState.update { it.copy(uplinkError = null) }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(uplinkError = "Uplink offline. Batcomputer awakening server...")
                }
            } catch (e: HttpException) {
                _uiState.update {
                    it.copy(uplinkError = "Server error — HTTP ${e.code()}. Using local data.")
                }
            }
        }
    }

    fun clearUplinkError() = _uiState.update { it.copy(uplinkError = null) }

    // ── Filters ───────────────────────────────────────────────────────────────
    private fun applyFilters() {
        val s = _uiState.value
        val filtered = _allMissions.value.filter { m ->
            (s.filterStatus == "ALL" || m.status == s.filterStatus) &&
            (s.filterPriority == "ALL" || m.priority == s.filterPriority) &&
            (s.filterCategory == "ALL" || m.category == s.filterCategory)
        }
        _uiState.update { it.copy(missions = filtered) }
    }

    fun setStatusFilter(status: String) { _uiState.update { it.copy(filterStatus = status) }; applyFilters() }
    fun setPriorityFilter(priority: String) { _uiState.update { it.copy(filterPriority = priority) }; applyFilters() }
    fun setCategoryFilter(category: String) { _uiState.update { it.copy(filterCategory = category) }; applyFilters() }

    // ── CRUD ─────────────────────────────────────────────────────────────────
    fun openAddDialog() = _uiState.update { it.copy(isAddDialogOpen = true, editingMission = null) }
    fun openEditDialog(m: MissionEntity) = _uiState.update { it.copy(isAddDialogOpen = true, editingMission = m) }
    fun closeDialog() = _uiState.update { it.copy(isAddDialogOpen = false, editingMission = null) }

    fun saveMission(
        title: String, description: String, priority: String,
        status: String, category: String, dueDate: Long?
    ) {
        viewModelScope.launch {
            val existing = _uiState.value.editingMission
            if (existing != null) {
                dao.updateMission(existing.copy(title = title, description = description,
                    priority = priority, status = status, category = category, dueDate = dueDate))
            } else {
                dao.insertMission(MissionEntity(title = title, description = description,
                    priority = priority, status = status, category = category, dueDate = dueDate))
            }
            closeDialog()
        }
    }

    fun deleteMission(mission: MissionEntity) { viewModelScope.launch { dao.deleteMission(mission) } }
    fun updateMissionStatus(mission: MissionEntity, newStatus: String) {
        viewModelScope.launch { dao.updateMission(mission.copy(status = newStatus)) }
    }

    companion object {
        fun factory(dao: MissionDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = MissionsViewModel(dao) as T
            }
    }
}
