package com.example.bookshelf

import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call

interface CountryService {
    @GET("/b/IU1K")
    fun getCountries(): Call<List<Country>>
}

object RetrofitInstance {
    private const val BASE_URL = "https://www.jsonkeeper.com"

    val api: CountryService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryService::class.java)
    }
}
