package com.omarkrostom.azanEdge.oldApp

import android.app.Application
import android.content.IntentFilter
import com.bugsnag.android.Bugsnag
import com.omarkrostom.azanEdge.oldApp.widgetProviders.AzanEdgeHorizontalProvider
import com.omarkrostom.azanEdge.oldApp.widgetProviders.AzanEdgeSinglePlusProvider

class AzanEdgeApplication : Application() {

    private val EDGE_PLUS_BROADCAST_RECEIVER = "com.samsung.android.cocktail.v2.action.COCKTAIL_UPDATE"


    private val AZAN_EDGE_HORIZONTAL_BROADCAST_RECEIVER = "android.appwidget.action.APPWIDGET_UPDATE"

    private val mEdgeSinglePlusProvider = AzanEdgeSinglePlusProvider()
    private val mAzanEdgeHorizontalProvider = AzanEdgeHorizontalProvider()

    override fun onCreate() {
        super.onCreate()

        /*Initialize Error Reporting tool*/
        initializeErrorReportingTool()

        /*Register AzanEdgeSinglePlusProvider and PrayerTimesProvider*/
        registerReceiver(mEdgeSinglePlusProvider, IntentFilter(EDGE_PLUS_BROADCAST_RECEIVER))

        /*Register AzanEdgeHorizontalProvider*/
        registerReceiver(mAzanEdgeHorizontalProvider, IntentFilter(AZAN_EDGE_HORIZONTAL_BROADCAST_RECEIVER))
    }

    override fun onTerminate() {
        super.onTerminate()

        /*Unregister AzanEdgeSinglePlusProvider and PrayerTimesProvider*/
        unregisterReceiver(mEdgeSinglePlusProvider)

        /*Unregister AzanEdgeSinglePlusProvider and PrayerTimesProvider*/
        unregisterReceiver(mAzanEdgeHorizontalProvider)
    }

    private fun initializeErrorReportingTool() {
        Bugsnag.init(this)
    }

}