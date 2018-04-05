package com.omarkrostom.azanEdge.networking

import com.omarkrostom.azanEdge.Config
import com.omarkrostom.azanEdge.networking.models.PrayerResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface AzanRetrofitInterface {

    @Headers("X-Mashape-Key: " + Config.PRAYER_TIMES_AUTH_KEY,
            "Accept: application/json")
    @GET("timingsByCity?")
    fun getTimingsByCity(@Query("city") city: String,
                         @Query("country") country: String,
                         @Query("method") method: String): Call<PrayerResponse>

}