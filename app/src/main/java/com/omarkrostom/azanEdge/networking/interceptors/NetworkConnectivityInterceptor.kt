package com.omarkrostom.azanEdge.networking.interceptors

import com.omarkrostom.azanEdge.networking.exceptions.NoInternetException
import com.omarkrostom.azanEdge.utils.isInternetConnected
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by omarkrostom on 3/15/18.
 */
class NetworkConnectivityInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain?): Response? {
        val request = chain?.request()

        if (!isInternetConnected()) {
            throw NoInternetException()
        }

        return chain?.proceed(request)
    }

}