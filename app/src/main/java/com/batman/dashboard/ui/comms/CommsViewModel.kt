package com.batman.dashboard.ui.comms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batman.dashboard.data.api.MessageRequest
import com.batman.dashboard.data.api.MessageResponse
import com.batman.dashboard.data.api.NetworkModule
import com.batman.dashboard.data.db.MessageDao
import com.batman.dashboard.data.db.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class Ally(
    val id: String,
    val name: String,
    val role: String,
    val iconKey: String,
    val isOnline: Boolean,
)

val ALLIES = listOf(
    Ally("alfred",    "Alfred Pennyworth", "Butler & Field Support", "alfred",    true),
    Ally("robin",     "Robin (Tim Drake)", "Combat Partner",         "robin",     true),
    Ally("nightwing", "Nightwing",         "Blüdhaven Operative",   "nightwing", false),
    Ally("oracle",    "Oracle (Barbara)",  "Intelligence & Hacking", "oracle",    true),
    Ally("gordon",    "Comm. Gordon",      "GCPD Liaison",           "gordon",    false),
    Ally("lucius",    "Lucius Fox",        "Wayne Tech Director",    "lucius",    true),
)

data class CommsUiState(
    val allies: List<Ally> = ALLIES,
    val messages: Map<String, List<MessageEntity>> = emptyMap(),
    val currentAllyId: String? = null,
    val networkError: String? = null,
    val isTyping: Boolean = false,
)

class CommsViewModel(private val dao: MessageDao) : ViewModel() {

    private val _uiState = MutableStateFlow(CommsUiState())
    val uiState: StateFlow<CommsUiState> = _uiState.asStateFlow()

    fun loadMessages(allyId: String): Flow<List<MessageEntity>> =
        dao.getMessagesForAlly(allyId)

    fun sendMessage(allyId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            dao.insertMessage(MessageEntity(allyId = allyId, content = content, isFromBatman = true))
            _uiState.update { it.copy(isTyping = true) }
            try {
                val response: MessageResponse = NetworkModule.api.sendMessage(
                    MessageRequest(sender = "Batman", content = content)
                )
                val reply = response.aiResponse ?: (allyReplies[allyId]?.random() ?: "OK.")
                dao.insertMessage(MessageEntity(allyId = allyId, content = reply, isFromBatman = false))
                _uiState.update { it.copy(networkError = null) }
            } catch (e: Exception) {
                val msg = when (e) {
                    is IOException   -> "Connection error. Retrying..."
                    is HttpException -> "Server error (${e.code()})."
                    else             -> "Something went wrong."
                }
                _uiState.update { it.copy(networkError = msg) }
                fallbackReply(allyId)
            } finally {
                _uiState.update { it.copy(isTyping = false) }
            }
        }
    }

    private suspend fun fallbackReply(allyId: String) {
        kotlinx.coroutines.delay(800L)
        val reply = (allyReplies[allyId] ?: listOf("Got it.")).random()
        dao.insertMessage(MessageEntity(allyId = allyId, content = reply, isFromBatman = false))
    }

    fun clearNetworkError() = _uiState.update { it.copy(networkError = null) }

    companion object {
        fun factory(dao: MessageDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CommsViewModel(dao) as T
            }

        val allyReplies = mapOf(
            "alfred"    to listOf("Very good, sir. I've prepared everything you'll need.", "Shall I notify Master Dick as well?"),
            "robin"     to listOf("On it! I'll back you up from the east flank.", "Copy that!"),
            "nightwing" to listOf("I'm working a case in Blüdhaven, but I can be there in 20.", "Signal received. Moving to intercept."),
            "oracle"    to listOf("Already hacked their comms. Sending you the data now.", "Cross-referencing with GCPD database... found a match."),
            "gordon"    to listOf("I've got half the force on standby.", "We found something at the scene you need to see."),
            "lucius"    to listOf("The new suit upgrade is ready for field testing.", "R&D has something that might help with that."),
        )
    }
}
