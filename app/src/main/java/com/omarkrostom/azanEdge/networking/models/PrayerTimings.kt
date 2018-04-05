package com.omarkrostom.azanEdge.networking.models

import com.google.gson.annotations.SerializedName

data class PrayerTimings(
        @SerializedName("Fajr") var fajr: String,
        @SerializedName("Sunrise") var sunrise: String,
        @SerializedName("Dhuhr") var zuhr: String,
        @SerializedName("Asr") var asr: String,
        @SerializedName("Maghrib") var maghrib: String,
        @SerializedName("Isha") var isha: String
)