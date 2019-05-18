package com.omarkrostom.azanEdge.oldApp.networking.models

import com.google.gson.annotations.SerializedName

data class PrayerResponse(
        @SerializedName("code") var code: Int,
        @SerializedName("status") var status: String,
        @SerializedName("data") var data: PrayerData
)