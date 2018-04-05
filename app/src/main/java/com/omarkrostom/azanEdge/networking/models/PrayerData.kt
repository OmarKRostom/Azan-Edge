package com.omarkrostom.azanEdge.networking.models

import com.google.gson.annotations.SerializedName

data class PrayerData(
        @SerializedName("timings") var prayerTimings: PrayerTimings
)