package com.omarkrostom.azanEdge.oldApp.networking.models

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class PrayersDate(

        @SerializedName("hijri") var hijriDate: JsonObject

)