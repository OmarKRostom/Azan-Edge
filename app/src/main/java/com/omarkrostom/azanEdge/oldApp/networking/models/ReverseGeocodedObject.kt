package com.omarkrostom.azanEdge.oldApp.networking.models

import com.google.gson.annotations.SerializedName

data class ReverseGeocodedObject(

        @SerializedName("formatted_address") var formattedAddress: String

)