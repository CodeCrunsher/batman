package com.batman.dashboard.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ── Response / request models ─────────────────────────────────────────────────

@Serializable
data class MissionResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    val priority: Int = 2,
    @SerialName("is_completed") val isCompleted: Int = 0
)

@Serializable
data class EquipmentResponse(
    val id: Int,
    val name: String,
    val status: String = "OPERATIONAL",
    @SerialName("integrity_level") val integrityLevel: Int = 100
)

@Serializable
data class MessageRequest(
    val sender: String,
    val content: String
)

/**
 * Backend POST /messages now returns a single object with one relevant field.
 * ignoreUnknownKeys = true in NetworkModule means any extra fields are safely ignored.
 */
@Serializable
data class MessageResponse(
    val aiResponse: String? = null
)

// ── Retrofit interface ────────────────────────────────────────────────────────

interface BatcomputerApi {

    // Missions
    @GET("missions")
    suspend fun getMissions(): List<MissionResponse>

    @GET("missions/{id}")
    suspend fun getMission(@Path("id") id: Int): MissionResponse

    @PATCH("missions/{id}/complete")
    suspend fun completeMission(@Path("id") id: Int): Map<String, String>

    // Equipment
    @GET("equipment")
    suspend fun getEquipment(): List<EquipmentResponse>

    // Comms / Oracle AI
    @POST("/messages")
    suspend fun sendMessage(@Body request: MessageRequest): MessageResponse

    @GET("messages")
    suspend fun getMessages(@Query("limit") limit: Int = 50): List<MessageResponse>

    // Health check
    @GET("/")
    suspend fun ping(): Map<String, String>
}
