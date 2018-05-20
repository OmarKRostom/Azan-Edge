package com.omarkrostom.azanEdge.networking.models

import com.google.gson.annotations.SerializedName

data class ReverseGeocodedObject(

        @SerializedName("formatted_address") var formattedAddress: String

)