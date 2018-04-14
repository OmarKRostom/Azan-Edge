package com.omarkrostom.azanEdge

import android.app.Application
import android.content.ComponentName
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Handler
import android.preference.PreferenceManager
import android.widget.Toast
import com.omarkrostom.azanEdge.broadcastReceivers.EdgeSinglePlusProvider
import com.omarkrostom.azanEdge.networking.PrayersApiManager
import com.omarkrostom.azanEdge.networking.models.PrayerResponse
import com.omarkrostom.azanEdge.utils.get
import com.omarkrostom.azanEdge.utils.set
import com.omarkrostom.azanEdge.utils.setPrayerTimes
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager

class AzanEdgeApplication : Application() {

    private val EDGE_PLUS_BROADCAST_RECEIVER = "com.samsung.android.cocktail.v2.action.COCKTAIL_UPDATE"

    private val mEdgeSinglePlusProvider = EdgeSinglePlusProvider()

    override fun onCreate() {
        super.onCreate()

        /*Register EdgeSinglePlusProvider and PrayerTimesProvider*/
        registerReceiver(mEdgeSinglePlusProvider, IntentFilter(EDGE_PLUS_BROADCAST_RECEIVER))
    }

    override fun onTerminate() {
        super.onTerminate()

        /*Unregister EdgeSinglePlusProvider and PrayerTimesProvider*/
        unregisterReceiver(mEdgeSinglePlusProvider)
    }

}