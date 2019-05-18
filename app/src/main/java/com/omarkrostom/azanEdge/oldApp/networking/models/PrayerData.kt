package com.omarkrostom.azanEdge.oldApp.networking.models

import com.google.gson.annotations.SerializedName

data class PrayerData(
        @SerializedName("timings") var prayerTimings: PrayerTimings,
        @SerializedName("date") var prayersDate: PrayersDate
)