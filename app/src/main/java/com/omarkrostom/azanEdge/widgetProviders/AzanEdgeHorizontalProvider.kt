package com.omarkrostom.azanEdge.widgetProviders

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.omarkrostom.azanEdge.Constants
import com.omarkrostom.azanEdge.R
import com.omarkrostom.azanEdge.networking.networkManagers.PrayersApiManager
import com.omarkrostom.azanEdge.networking.models.PrayerResponse
import com.omarkrostom.azanEdge.utils.get
import com.omarkrostom.azanEdge.utils.set
import com.omarkrostom.azanEdge.utils.setPrayerTimes

class AzanEdgeHorizontalProvider : AppWidgetProvider(), PrayersApiManager.OnPrayerTimesApiListener {

    private lateinit var mAppWidgetIds: IntArray
    private lateinit var mContext: Context
    private lateinit var mPreferenceManager: SharedPreferences

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        /* Update context instance */
        mContext = context

        /* Initialize PreferencesManager */
        mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(mContext)

        /* Set MainPlusLayout */
        setMainPlusLayout(mContext, appWidgetManager, appWidgetIds)

        /* UpdatePrayerTimes */
        getPrayerTimesFromApi(mPreferenceManager)
    }

    override fun onDisabled(context: Context?) {}

    override fun onSuccess(prayerResponse: PrayerResponse) {
        /* Save response to preferences */
        mPreferenceManager.set(Constants.FAJR_PRAYER, prayerResponse.data.prayerTimings.fajr)
        mPreferenceManager.set(Constants.ZUHR_PRAYER, prayerResponse.data.prayerTimings.zuhr)
        mPreferenceManager.set(Constants.ASR_PRAYER, prayerResponse.data.prayerTimings.asr)
        mPreferenceManager.set(Constants.MAGHRIB_PRAYER, prayerResponse.data.prayerTimings.maghrib)
        mPreferenceManager.set(Constants.ISHA_PRAYER, prayerResponse.data.prayerTimings.isha)

        updateWidget()
    }

    override fun onError(error: String) {
        Log.d("Something went wrong: ", error)
    }

    private fun updateWidget() {
        mAppWidgetIds = AppWidgetManager.getInstance(mContext).getAppWidgetIds(
                ComponentName(mContext, AzanEdgeHorizontalProvider::class.java)
        )

        if (mAppWidgetIds.isNotEmpty()) {
            setPrayerTimes(mContext.packageName,
                    mAppWidgetIds,
                    R.layout.layout_main_horizontal,
                    mContext,
                    false)
        }
    }

    /* Enables main layout */
    private fun setMainPlusLayout(context: Context, manager: AppWidgetManager, cocktailIds: IntArray?) {
        val layoutId = R.layout.layout_main_horizontal
        val rv = RemoteViews(context.packageName, layoutId)
        rv.setViewVisibility(R.id.ll_main_app, View.VISIBLE)
        updateIds(
                cocktailIds,
                manager,
                rv
        )
        setPrayerTimes(context.packageName,
                cocktailIds!!,
                R.layout.layout_main_horizontal,
                context,
                false)
    }

    /* Needed to update view */
    private fun updateIds(cocktailIds: IntArray?,
                          manager: AppWidgetManager,
                          rv: RemoteViews) {
        if (cocktailIds != null) {
            for (id in cocktailIds) {
                manager.updateAppWidget(id, rv)
            }
        }
    }

    /* Needed to retreive prayer times */
    private fun getPrayerTimesFromApi(sharedPreferences: SharedPreferences = mPreferenceManager) {
        PrayersApiManager.getPrayerTimesFromApi(
                sharedPreferences.get(Constants.APP_LAT, Constants.DEFAULT_LAT).toString(),
                sharedPreferences.get(Constants.APP_LONG, Constants.DEFAULT_LONG).toString(),
                ((sharedPreferences.get(Constants.AZAN_METHOD_INDEX, Constants.DEFAULT_METHOD_INDEX) as Int) + 1).toString(),
                this::onSuccess,
                this::onError
        )
    }

}