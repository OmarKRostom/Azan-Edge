package com.omarkrostom.azanEdge.oldApp.networking.interceptors

import com.omarkrostom.azanEdge.oldApp.networking.exceptions.NoInternetException
import com.omarkrostom.azanEdge.oldApp.utils.isInternetConnected
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