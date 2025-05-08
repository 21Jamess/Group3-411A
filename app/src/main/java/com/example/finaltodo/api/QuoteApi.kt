package com.example.finaltodo.api

import retrofit2.Call
import retrofit2.http.GET

interface QuoteApi {
    @GET("api/random")
    fun getRandomQuote(): Call<List<Quote>>
}