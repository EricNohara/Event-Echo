package com.example.eventecho.data.api.ticketmaster

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TicketmasterClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // logs full request/response
    }

    // Build OkHttpClient and attach the logging interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://app.ticketmaster.com/")
        .client(client) // attach the client here
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: TicketmasterApi = retrofit.create(TicketmasterApi::class.java)
}