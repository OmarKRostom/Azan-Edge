package com.omarkrostom.azanEdge.widgetProviders

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
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider

class AzanEdgeSinglePlusProvider : SlookCocktailProvider(), PrayersApiManager.OnPrayerTimesApiListener {

    private lateinit var mCocktailIds: IntArray
    private lateinit var mContext: Context
    private lateinit var mPreferenceManager: SharedPreferences

    override fun onUpdate(context: Context, cocktailManager: SlookCocktailManager, cocktailIds: IntArray?) {
        /* Update context instance */
        mContext = context

        /* Initialize PreferencesManager */
        mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(mContext)

        /* Set MainPlusLayout */
        setMainPlusLayout(mContext, cocktailManager, cocktailIds)

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
        mCocktailIds = SlookCocktailManager.getInstance(mContext).getCocktailIds(
                ComponentName(mContext, AzanEdgeSinglePlusProvider::class.java)
        )

        if (mCocktailIds.isNotEmpty()) {
            setPrayerTimes(mContext.packageName,
                    mCocktailIds,
                    R.layout.layout_main_vertical,
                    mContext,
                    true)
        }
    }

    /* Enables main layout */
    private fun setMainPlusLayout(context: Context, manager: SlookCocktailManager, cocktailIds: IntArray?) {
        val layoutId = R.layout.layout_main_vertical
        val rv = RemoteViews(context.packageName, layoutId)
        rv.setViewVisibility(R.id.ll_main_app, View.VISIBLE)
        updateCocktails(
                cocktailIds,
                manager,
                rv
        )
        setPrayerTimes(mContext.packageName,
                cocktailIds!!,
                R.layout.layout_main_vertical,
                context,
                true)
    }

    /* Needed to update view */
    private fun updateCocktails(cocktailIds: IntArray?,
                                manager: SlookCocktailManager,
                                rv: RemoteViews) {
        if (cocktailIds != null) {
            for (id in cocktailIds) {
                manager.updateCocktail(id, rv)
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