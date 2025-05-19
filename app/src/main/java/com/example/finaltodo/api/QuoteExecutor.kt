package com.example.finaltodo.api

import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuoteExecutor {

    private val quoteApi: QuoteApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://zenquotes.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        quoteApi = retrofit.create(QuoteApi::class.java)
    }

    fun fetchQuote(): LiveData<Quote> {
        val responseLiveData: MutableLiveData<Quote> = MutableLiveData()
        val quoteRequest: Call<List<Quote>> = quoteApi.getRandomQuote()

        quoteRequest.enqueue(object : Callback<List<Quote>> {
            override fun onFailure(call: Call<List<Quote>>, t: Throwable) {
                Log.e("QuoteExecutor","failed to fetch Quote", t)
            }

            override fun onResponse(call: Call<List<Quote>>, response: Response<List<Quote>>) {
                Log.d("QuoteExecutor","Response received from Quote endpoint")

                val quoteList: List<Quote>? = response.body()
                val quote: Quote? = quoteList?.firstOrNull()

                if (quote != null && quote.q.isNotBlank()) {
                    responseLiveData.value = quote
                }
                else {
                    Log.w("QuoteExecutor","Invalid Quote Recieved")
                }
            }
        })
        return responseLiveData
    }


}