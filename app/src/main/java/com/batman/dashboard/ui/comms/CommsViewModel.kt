package com.batman.dashboard.ui.comms

import androidx.lifecycle.*
import com.batman.dashboard.data.api.MessageRequest
import com.batman.dashboard.data.api.MessageResponse
import com.batman.dashboard.data.api.NetworkModule
import com.batman.dashboard.data.db.MessageDao
import com.batman.dashboard.data.db.MessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class Ally(
    val id: String,
    val name: String,
    val role: String,
    val iconKey: String,
    val isOnline: Boolean
)

val ALLIES = listOf(
    Ally("alfred",    "Alfred Pennyworth",   "Butler & Field Support",     "alfred",   true),
    Ally("robin",     "Robin (Tim Drake)",   "Combat Partner",             "robin",    true),
    Ally("nightwing", "Nightwing",           "Blüdhaven Operative",        "nightwing", false),
    Ally("oracle",    "Oracle (Barbara)",    "Intelligence & Hacking",     "oracle",   true),
    Ally("gordon",    "Comm. Gordon",        "GCPD Liaison",               "gordon",   false),
    Ally("lucius",    "Lucius Fox",          "Wayne Tech Director",        "lucius",   true),
)

data class CommsUiState(
    val allies: List<Ally> = ALLIES,
    val messages: Map<String, List<MessageEntity>> = emptyMap(),
    val currentAllyId: String? = null,
    val uplinkError: String? = null
)

class CommsViewModel(private val dao: MessageDao) : ViewModel() {

    private val _uiState = MutableStateFlow(CommsUiState())
    val uiState: StateFlow<CommsUiState> = _uiState.asStateFlow()

    fun loadMessages(allyId: String): Flow<List<MessageEntity>> =
        dao.getMessagesForAlly(allyId)

    fun sendMessage(allyId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            // 1. Save Batman's message immediately
            dao.insertMessage(
                MessageEntity(allyId = allyId, content = content, isFromBatman = true)
            )

            try {
                // 2. EVERY ally hits the live Python backend
                val response: MessageResponse = NetworkModule.api.sendMessage(
                    MessageRequest(sender = "Batman", content = content)
                )

                // 3. ai_response field carries the Oracle/Gemini reply
                val aiReply = response.aiResponse
                    ?: (allyReplies[allyId]?.random() ?: "Signal nominal.")

                dao.insertMessage(
                    MessageEntity(allyId = allyId, content = aiReply, isFromBatman = false)
                )

                // Clear any previous error banner
                _uiState.update { it.copy(uplinkError = null) }

            } catch (e: Exception) {
                // 4. Fallback if the server is offline or waking up
                val errorMsg = when (e) {
                    is IOException   -> "Uplink offline. Batcomputer awakening server..."
                    is HttpException -> "Batcomputer error — HTTP ${e.code()}. Rerouting."
                    else             -> "Data corruption detected. Fallback engaged."
                }

                _uiState.update { it.copy(uplinkError = errorMsg) }
                fallbackReply(allyId)
            }
        }
    }

    private suspend fun fallbackReply(allyId: String) {
        kotlinx.coroutines.delay(800L)
        val reply = (allyReplies[allyId] ?: listOf("Signal lost. Standing by.")).random()
        dao.insertMessage(MessageEntity(allyId = allyId, content = reply, isFromBatman = false))
    }

    fun clearUplinkError() = _uiState.update { it.copy(uplinkError = null) }

    companion object {
        fun factory(dao: MessageDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CommsViewModel(dao) as T
            }

        val allyReplies = mapOf(
            "alfred"    to listOf("Very good, sir. I've prepared everything you'll need.", "Shall I notify Master Dick as well?"),
            "robin"     to listOf("On it! I'll back you up from the east flank.", "Copy that! Holy encrypted signal, Batman!"),
            "nightwing" to listOf("I'm working a case in Blüdhaven, but I can be there in 20.", "Signal received. Moving to intercept."),
            "oracle"    to listOf("I've already hacked their comms. Sending you the data now.", "Cross-referencing with GCPD database... found a match."),
            "gordon"    to listOf("Batman. I've got half the force on standby.", "We found something at the scene you need to see."),
            "lucius"    to listOf("The new suit upgrade is ready for field testing, Mr. Wayne.", "Wayne Industries R&D has something that might help with that.")
        )
    }
}
