package com.example.colegio.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Servidor Spring Boot desplegado en Render
    private const val BASE_URL_SPRING = "https://appsmoviles.onrender.com/"

    // API externa pública de feriados legales de Chile
    private const val BASE_URL_FERIADOS = "https://date.nager.at/"

    // Timeout ampliado a 45s para soportar el arranque en frío (cold start) de Render
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    val colegioApi: ColegioApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SPRING)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ColegioApiService::class.java)
    }

    val feriadosApi: FeriadosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_FERIADOS)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FeriadosApiService::class.java)
    }
}