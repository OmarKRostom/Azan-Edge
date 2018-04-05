package com.omarkrostom.azanEdge.networking

import com.omarkrostom.azanEdge.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkFactory {

    var okHttpClient: OkHttpClient? = null

    fun <T> makeNetworkRequest(networkCall: Call<T>,
                               onSuccess: (T) -> Unit?,
                               onError: (String) -> Unit?) {
        networkCall.enqueue(object : Callback<T> {

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.body() != null) {
                    onSuccess(response.body()!!)
                } else {
                    onError(Constants.SOMETHING_WENT_WRONG)
                }
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                onError(throwable.localizedMessage)
            }

        })
    }

    fun <S> getRetrofitServiceInstance(baseUrl: String, serviceClass: Class<S>): S {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpBuilder())
                .build()
                .create(serviceClass)
    }

    private fun getOkHttpBuilder(): OkHttpClient? {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build()
        }
        return okHttpClient
    }

}