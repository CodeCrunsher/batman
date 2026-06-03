package com.batman.dashboard.ui.emergency

import androidx.lifecycle.*
import com.batman.dashboard.data.db.EmergencyContactEntity
import com.batman.dashboard.data.db.EmergencyDao
import com.batman.dashboard.data.db.EmergencyLogEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CallMessage(val chip: String, val response: String)

data class EmergencyUiState(
    val contacts: List<EmergencyContactEntity> = emptyList(),
    val alertLog: List<EmergencyLogEntity> = emptyList(),
    val isSosActive: Boolean = false,
    val sosCountdown: Int = 0,
    val selectedAlertType: String = "TACTICAL SUPPORT",
    val lastAlertSentAt: Long? = null,
    val showSuccess: Boolean = false,
    // ── Call simulation ──
    val activeCall: EmergencyContactEntity? = null,
    val callDurationSeconds: Int = 0,
    val callMessages: List<CallMessage> = emptyList(),
    /** Chips specific to the active contact — set by startCall() */
    val activeCallChips: List<String> = emptyList()
)

class EmergencyViewModel(private val dao: EmergencyDao) : ViewModel() {
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    private var sosJob: Job? = null
    private var callTimerJob: Job? = null

    init {
        viewModelScope.launch {
            launch { dao.getActiveContacts().collect { c -> _uiState.update { s -> s.copy(contacts = c) } } }
            launch { dao.getRecentAlerts().collect { l -> _uiState.update { s -> s.copy(alertLog = l) } } }
        }
    }

    fun selectAlertType(type: String) = _uiState.update { it.copy(selectedAlertType = type) }

    // ── Simulated call ────────────────────────────────────────────────────────

    fun startCall(contact: EmergencyContactEntity) {
        callTimerJob?.cancel()
        val chips = contactChips[contact.name] ?: defaultChips
        _uiState.update {
            it.copy(activeCall = contact, callDurationSeconds = 0,
                callMessages = emptyList(), activeCallChips = chips)
        }
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { it.copy(callDurationSeconds = it.callDurationSeconds + 1) }
            }
        }
    }

    fun endCall() {
        callTimerJob?.cancel()
        _uiState.update {
            it.copy(activeCall = null, callDurationSeconds = 0,
                callMessages = emptyList(), activeCallChips = emptyList())
        }
    }

    fun sendQuickMessage(chip: String) {
        val contactName = _uiState.value.activeCall?.name ?: return
        val response = quickResponses[chip]?.get(contactName)
            ?: quickResponses[chip]?.get("default")
            ?: "Copy that, Batman."
        _uiState.update { it.copy(callMessages = it.callMessages + CallMessage(chip, response)) }
    }

    // ── SOS ───────────────────────────────────────────────────────────────────

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
        _uiState.update {
            it.copy(isSosActive = false, sosCountdown = 0,
                lastAlertSentAt = System.currentTimeMillis(), showSuccess = true)
        }
        delay(3000L)
        _uiState.update { it.copy(showSuccess = false) }
    }

    companion object {
        fun factory(dao: EmergencyDao): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = EmergencyViewModel(dao) as T
        }

        /** Fallback chips for contacts not in the map below. */
        val defaultChips = listOf(
            "I need help!",
            "Bring the Batmobile",
            "Deploy countermeasures",
            "Extraction requested"
        )

        /** Per-contact quick-message chips shown in the call dialog. */
        val contactChips = mapOf(
            "Alfred Pennyworth" to listOf(
                "Is the Batmobile ready?",
                "Prepare the medical bay",
                "Secure the cave",
                "I need extraction"
            ),
            "Robin (Tim Drake)" to listOf(
                "Cover the east flank",
                "Move in on my signal",
                "Need backup now!",
                "Abort — fall back"
            ),
            "Commissioner Gordon" to listOf(
                "Clear the area",
                "I need a diversion",
                "Seal the perimeter",
                "Stand down your units"
            ),
            "Lucius Fox" to listOf(
                "Remote-launch Batmobile",
                "Activate the EMP",
                "Override their systems",
                "Deploy the Batwing"
            )
        )

        /** Response per chip per contact. Falls back to \"default\" key if no exact match. */
        val quickResponses: Map<String, Map<String, String>> = mapOf(
            // ── Alfred ──
            "Is the Batmobile ready?" to mapOf(
                "Alfred Pennyworth" to "Fully fuelled and armed, Master Bruce. She's waiting.",
                "default" to "Checking vehicle status now."
            ),
            "Prepare the medical bay" to mapOf(
                "Alfred Pennyworth" to "Medical bay is ready. Please don't make it worse this time, sir.",
                "default" to "Medical support en route."
            ),
            "Secure the cave" to mapOf(
                "Alfred Pennyworth" to "Cave locked down. Biometric seals active — no one gets in.",
                "default" to "Location secured."
            ),
            "I need extraction" to mapOf(
                "Alfred Pennyworth" to "Extraction vehicle en route, Master Bruce. Six minutes.",
                "Robin (Tim Drake)" to "Swing east — I've got your exit covered!",
                "Lucius Fox" to "Batwing dispatched. Rooftop extraction in 2 minutes.",
                "Commissioner Gordon" to "Secure vehicle inbound. ETA 5 minutes.",
                "default" to "Extraction confirmed. Hold position."
            ),
            // ── Robin ──
            "Cover the east flank" to mapOf(
                "Robin (Tim Drake)" to "East flank locked. They won't get through me.",
                "default" to "Covering flank now."
            ),
            "Move in on my signal" to mapOf(
                "Robin (Tim Drake)" to "Ready and waiting. Just say the word.",
                "default" to "Standing by for your signal."
            ),
            "Need backup now!" to mapOf(
                "Robin (Tim Drake)" to "30 seconds out! Hold on!",
                "Commissioner Gordon" to "Units inbound. ETA 90 seconds, Batman.",
                "default" to "Backup mobilised. Inbound."
            ),
            "Abort — fall back" to mapOf(
                "Robin (Tim Drake)" to "Understood. Pulling back to rally point Bravo.",
                "default" to "Acknowledged. Withdrawing now."
            ),
            // ── Gordon ──
            "Clear the area" to mapOf(
                "Commissioner Gordon" to "Civilians cleared. You've got a 10-minute window.",
                "default" to "Area cleared."
            ),
            "I need a diversion" to mapOf(
                "Commissioner Gordon" to "Three patrol cars inbound — that'll keep eyes off you.",
                "default" to "Diversion underway."
            ),
            "Seal the perimeter" to mapOf(
                "Commissioner Gordon" to "All exits sealed. Nothing leaves that block.",
                "default" to "Perimeter sealed."
            ),
            "Stand down your units" to mapOf(
                "Commissioner Gordon" to "Units standing down. The scene is yours, Batman.",
                "default" to "Units recalled."
            ),
            // ── Lucius ──
            "Remote-launch Batmobile" to mapOf(
                "Lucius Fox" to "Remote launch initiated. Track goes live in 90 seconds.",
                "Alfred Pennyworth" to "Batmobile deployed, Master Bruce. Track active.",
                "default" to "Batmobile en route."
            ),
            "Activate the EMP" to mapOf(
                "Lucius Fox" to "EMP pulse fired. 40-metre radius down. Move now.",
                "default" to "EMP activated."
            ),
            "Override their systems" to mapOf(
                "Lucius Fox" to "I'm in their network. Cameras looped. You've got 90 seconds.",
                "default" to "Systems override in progress."
            ),
            "Deploy the Batwing" to mapOf(
                "Lucius Fox" to "Batwing airborne. Arrival at your coordinates: 3 minutes.",
                "default" to "Batwing deployed."
            ),
            // ── Default fallback chips ──
            "I need help!" to mapOf(
                "Alfred Pennyworth" to "On my way, Master Bruce. ETA 4 minutes.",
                "Robin (Tim Drake)" to "Incoming! Cover position Alpha. Don't move.",
                "Commissioner Gordon" to "All units dispatched. Hold your position.",
                "default" to "Acknowledged. Mobilising now."
            ),
            "Bring the Batmobile" to mapOf(
                "Alfred Pennyworth" to "Batmobile deployed, Master Bruce. Track active.",
                "Lucius Fox" to "Remote launch initiated. Estimated 90 seconds.",
                "default" to "Batmobile en route. Signal locked on your position."
            ),
            "Deploy countermeasures" to mapOf(
                "Alfred Pennyworth" to "Countermeasures armed and deployed, sir.",
                "Robin (Tim Drake)" to "Smoke deployed on the east side. Move now!",
                "Lucius Fox" to "EMP pulse initiated. 30-metre radius cleared.",
                "Commissioner Gordon" to "Backup deployed. Perimeter secured.",
                "default" to "Countermeasures active. Window: 60 seconds."
            ),
            "Extraction requested" to mapOf(
                "Alfred Pennyworth" to "Extraction vehicle en route, Master Bruce.",
                "Robin (Tim Drake)" to "Swing east — I've got your exit covered!",
                "Lucius Fox" to "Batwing dispatched. Rooftop extraction in 2 minutes.",
                "Commissioner Gordon" to "Secure vehicle inbound. ETA 5 minutes.",
                "default" to "Extraction confirmed. Hold position."
            )
        )
    }
}
