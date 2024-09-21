package com.example.bookshelf

import retrofit2.http.GET
import retrofit2.Call

interface ApiService {
    @GET("/b/CNGI")
    fun getBooks(): Call<List<Book>>
}

object BookRetrofitInstance {
    val api: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://www.jsonkeeper.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
