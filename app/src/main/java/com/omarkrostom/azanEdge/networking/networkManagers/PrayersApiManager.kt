package com.omarkrostom.azanEdge.networking.networkManagers

import com.omarkrostom.azanEdge.Config
import com.omarkrostom.azanEdge.Constants
import com.omarkrostom.azanEdge.networking.apiServices.AzanRetrofitInterface
import com.omarkrostom.azanEdge.networking.models.PrayerResponse
import com.omarkrostom.azanEdge.networking.models.ReverseGeocodedObjectResponse

object PrayersApiManager {

    fun getPrayerTimesFromApi(longitude: String,
                              latitude: String,
                              method: String,
                              onSuccess: (PrayerResponse) -> Unit,
                              onError: (String) -> Unit) {
        val prayerAzanRetrofitInterface = NetworkFactory
                .getRetrofitServiceInstance(Config.PRAYER_TIMES_API,
                        AzanRetrofitInterface::class.java)

        val prayerCall = prayerAzanRetrofitInterface
                .getTimings(System.currentTimeMillis() / 1000L,
                        longitude,
                        latitude,
                        method
                )

        NetworkFactory.makeNetworkRequest(prayerCall, onSuccess, onError)
    }

    fun getLocationFormattedAddress(longitude: String,
                                    latitude: String,
                                    onSuccess: (ReverseGeocodedObjectResponse) -> Unit,
                                    onError: (String) -> Unit) {

        val getFormattedAddressInstance = NetworkFactory
                .getRetrofitServiceInstance(Config.GOOGLE_MAPS_API,
                        AzanRetrofitInterface::class.java
                )

        val formattedAddressCall = getFormattedAddressInstance.getFormattedAddress(
                "$latitude,$longitude",
                Config.GOOGLE_API_KEY,
                Constants.MAPS_RESULT_TYPE_LEVEL
        )

        NetworkFactory.makeNetworkRequest(formattedAddressCall, onSuccess, onError)

    }

    interface OnPrayerTimesApiListener {
        fun onSuccess(prayerResponse: PrayerResponse)
        fun onError(error: String)
    }

    interface OnFormattedLocationApiListener {
        fun onLocationSetSuccess(reverseGeocodedObjectResponse: ReverseGeocodedObjectResponse)
        fun onLocationSetError(error: String)
    }

}