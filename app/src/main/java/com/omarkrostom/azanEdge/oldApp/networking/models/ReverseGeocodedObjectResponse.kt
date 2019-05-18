package com.omarkrostom.azanEdge.oldApp.networking.models

import com.google.gson.annotations.SerializedName

data class ReverseGeocodedObjectResponse(

        @SerializedName("results") var addressComponents: ArrayList<ReverseGeocodedObject>

)