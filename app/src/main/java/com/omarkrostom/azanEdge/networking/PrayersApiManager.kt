package com.omarkrostom.azanEdge.networking

import com.omarkrostom.azanEdge.Config
import com.omarkrostom.azanEdge.networking.models.PrayerResponse

object PrayersApiManager {

    fun getPrayerTimesFromApi(city: String,
                              country: String,
                              method: String,
                              onSuccess: (PrayerResponse) -> Unit,
                              onError: (String) -> Unit) {
        val prayerAzanRetrofitInterface = NetworkFactory
                .getRetrofitServiceInstance(Config.PRAYER_TIMES_API, AzanRetrofitInterface::class.java)

        val prayerCall = prayerAzanRetrofitInterface.getTimingsByCity(city, country, method)

        NetworkFactory.makeNetworkRequest(prayerCall, onSuccess, onError)
    }

    interface OnPrayerTimesApiListener {
        fun onSuccess(prayerResponse: PrayerResponse)
        fun onError(error: String)
    }

}