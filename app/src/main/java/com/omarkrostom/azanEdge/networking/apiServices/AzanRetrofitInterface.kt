package com.omarkrostom.azanEdge.networking.apiServices

import com.omarkrostom.azanEdge.networking.models.PrayerResponse
import com.omarkrostom.azanEdge.networking.models.ReverseGeocodedObjectResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AzanRetrofitInterface {

    @GET("timings/{timeUnix}")
    fun getTimings(@Path("timeUnix") timeUnix: Long,
                   @Query("latitude") latitude: String,
                   @Query("longitude") longitude: String,
                   @Query("method") method: String): Call<PrayerResponse>

    @GET("geocode/json")
    fun getFormattedAddress(@Query("latlng") latlng: String,
                            @Query("key") key: String,
                            @Query("result_type") resultType: String): Call<ReverseGeocodedObjectResponse>

}