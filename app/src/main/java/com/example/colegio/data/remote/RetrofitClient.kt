package com.example.colegio.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 10.0.2.2 apunta al localhost de tu PC desde el emulador Android.
    // Si subes el Spring Boot a la nube (ej. Render), reemplaza esta URL por tu link HTTPS.
    private const val BASE_URL_SPRING = "https://appsmoviles.onrender.com/"

    // API externa pública de feriados legales de Chile
    private const val BASE_URL_FERIADOS = "https://date.nager.at/"

    val colegioApi: ColegioApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SPRING)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ColegioApiService::class.java)
    }

    val feriadosApi: FeriadosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_FERIADOS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FeriadosApiService::class.java)
    }
}