package com.omarkrostom.azanEdge.broadcastReceivers

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.omarkrostom.azanEdge.Config
import com.omarkrostom.azanEdge.Constants
import com.omarkrostom.azanEdge.R
import com.omarkrostom.azanEdge.networking.PrayersApiManager
import com.omarkrostom.azanEdge.networking.models.PrayerResponse
import com.omarkrostom.azanEdge.utils.get
import com.omarkrostom.azanEdge.utils.set
import com.omarkrostom.azanEdge.utils.setPrayerTimes
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider

class EdgeSinglePlusProvider : SlookCocktailProvider(), PrayersApiManager.OnPrayerTimesApiListener {

    private lateinit var mCocktailIds: IntArray
    private lateinit var mDailyRunnable: Runnable
    private lateinit var mContext: Context
    private lateinit var mPreferenceManager: SharedPreferences

    private val mHandler = Handler()

    override fun onUpdate(context: Context, cocktailManager: SlookCocktailManager, cocktailIds: IntArray?) {
        /* Update context instance */
        mContext = context

        /* Initialize PreferencesManager */
        mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(mContext)

        /* Schedule daily update task */
        schedulePrayerTimesUpdateTask()

        /* Set MainPlusLayout */
        setMainPlusLayout(mContext, cocktailManager, cocktailIds)
    }

    override fun onDisabled(context: Context?) {
        /* Stop scheduling prayer updates */
        mHandler.removeCallbacks(mDailyRunnable)
    }

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

    private fun schedulePrayerTimesUpdateTask() {
        mDailyRunnable = Runnable {
            schedulePrayerTask()
            mHandler.postDelayed(mDailyRunnable, Config.PRAYER_UPDATE_INTERVAL)
        }

        mHandler.post(mDailyRunnable)
    }

    private fun schedulePrayerTask() {
        PrayersApiManager.getPrayerTimesFromApi(
                mPreferenceManager.get(Constants.APP_COUNTRY, Constants.DEFAULT_COUNTRY).toString(),
                mPreferenceManager.get(Constants.APP_CITY, Constants.DEFAULT_CITY).toString(),
                ((mPreferenceManager.get(Constants.AZAN_METHOD_INDEX, Constants.DEFAULT_METHOD_INDEX) as Int) + 1).toString(),
                this::onSuccess,
                this::onError
        )
    }

    private fun updateWidget() {
        mCocktailIds = SlookCocktailManager.getInstance(mContext).getCocktailIds(
                ComponentName(mContext, EdgeSinglePlusProvider::class.java)
        )

        setPrayerTimes(mContext.packageName, mCocktailIds, mContext)
    }

    /* Enables main layout */
    private fun setMainPlusLayout(context: Context, manager: SlookCocktailManager, cocktailIds: IntArray?) {
        val layoutId = R.layout.layout_main
        val rv = RemoteViews(context.packageName, layoutId)
        rv.setViewVisibility(R.id.ll_main_app, View.VISIBLE)
        updateCocktails(
                cocktailIds,
                manager,
                rv
        )
        setPrayerTimes(context.packageName, cocktailIds!!, context)
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

}