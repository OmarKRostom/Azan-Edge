package com.omarkrostom.azanEdge.networking.networkManagers

import com.omarkrostom.azanEdge.NetworkConstants
import com.omarkrostom.azanEdge.networking.exceptions.NoInternetException
import com.omarkrostom.azanEdge.networking.interceptors.NetworkConnectivityInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Created by omarkrostom on 1/17/18.
 */
object NetworkFactory {

    var okHttpClient: OkHttpClient? = null

    private val networkHttpLogger by lazy {
        HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val networkConnectivityInterceptor by lazy {
        NetworkConnectivityInterceptor()
    }

    fun <T> makeNetworkRequest(networkCall: Call<T>,
                               onSuccess: (T) -> Unit?,
                               onError: (String) -> Unit?) {
        networkCall.enqueue(object : Callback<T> {

            override fun onResponse(call: Call<T>, response: Response<T>) {
                when (response.code()) {
                    HttpURLConnection.HTTP_UNAUTHORIZED ->
                        onError(NetworkConstants.NOT_LOGGED_IN)
                    HttpURLConnection.HTTP_INTERNAL_ERROR ->
                        onError(NetworkConstants.SOMETHING_WENT_WRONG)
                    HttpURLConnection.HTTP_OK ->
                        onSuccess(response.body()!!)
                }
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                when (throwable) {
                    is NoInternetException -> onError(NetworkConstants.NO_INTERNET_CONNECTION)
                    is SocketTimeoutException -> onError(NetworkConstants.TIME_OUT_EXCEPTION)
                }
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
                    .addInterceptor(networkHttpLogger)
                    .addInterceptor(networkConnectivityInterceptor)
                    .readTimeout(NetworkConstants.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(NetworkConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(NetworkConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .build()
        }
        return okHttpClient
    }

}