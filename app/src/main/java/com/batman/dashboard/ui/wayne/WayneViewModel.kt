package com.batman.dashboard.ui.wayne

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Meeting(
    val id: Long,
    val title: String,
    val location: String,
    val time: String
)

data class WayneUiState(
    val meetings: List<Meeting> = emptyList(),
    val isAddDialogOpen: Boolean = false
)

class WayneViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        WayneUiState(
            meetings = listOf(
                Meeting(1L, "Quarterly Board Review", "Wayne Tower — Boardroom A", "Tomorrow, 09:00 AM"),
                Meeting(2L, "R&D Budget Allocation", "Wayne Tech Lab — Floor 42", "Friday, 02:00 PM"),
                Meeting(3L, "Lucius Fox Tech Briefing", "Secure Lab — Sub-Level 3", "Monday, 11:30 AM"),
            )
        )
    )
    val uiState: StateFlow<WayneUiState> = _uiState.asStateFlow()

    fun openAddDialog() = _uiState.update { it.copy(isAddDialogOpen = true) }
    fun closeDialog() = _uiState.update { it.copy(isAddDialogOpen = false) }

    fun addMeeting(title: String, location: String, time: String) {
        if (title.isBlank()) return
        val meeting = Meeting(
            id = System.currentTimeMillis(),
            title = title,
            location = location.ifBlank { "Wayne Tower" },
            time = time.ifBlank { "TBD" }
        )
        _uiState.update { it.copy(meetings = it.meetings + meeting, isAddDialogOpen = false) }
    }

    fun deleteMeeting(id: Long) {
        _uiState.update { it.copy(meetings = it.meetings.filter { m -> m.id != id }) }
    }
}
