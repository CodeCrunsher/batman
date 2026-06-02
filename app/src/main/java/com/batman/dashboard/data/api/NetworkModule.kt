package com.batman.dashboard.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client pointing at the production Batcomputer backend.
 *
 * Timeouts are set to 60 s to accommodate Render free-tier cold-start delays
 * (the server may be sleeping and needs ~30-50 s to wake up on first request).
 */
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true   // safe against backend additions
        isLenient = true           // tolerates minor formatting differences
        coerceInputValues = true   // nulls coerced to defaults where possible
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /** OkHttpClient — 60-second timeouts on all operations. */
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    /** Live production Retrofit instance. */
    val api: BatcomputerApi = Retrofit.Builder()
        .baseUrl("https://backend-zret.onrender.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(BatcomputerApi::class.java)
}
