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

class AzanEdgeApplication : Application(), PrayersApiManager.OnPrayerTimesApiListener {

    private val EDGE_PLUS_BROADCAST_RECEIVER = "com.samsung.android.cocktail.v2.action.COCKTAIL_UPDATE"

    private lateinit var mPreferenceManager: SharedPreferences
    private lateinit var mCocktailIds: IntArray
    private lateinit var mDailyRunnable: Runnable

    private val mHandler = Handler()
    private val mEdgeSinglePlusProvider = EdgeSinglePlusProvider()

    override fun onCreate() {
        super.onCreate()

        /*Register EdgeSinglePlusProvider and PrayerTimesProvider*/
        registerReceiver(mEdgeSinglePlusProvider, IntentFilter(EDGE_PLUS_BROADCAST_RECEIVER))

        /* Initialize PreferencesManager */
        mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(this)

        /* Update Prayer Times From Preferences */
        updateWidget()

        /* Schedule daily update task */
        schedulePrayerTimesUpdateTask()
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

    override fun onTerminate() {
        super.onTerminate()

        /*Unregister EdgeSinglePlusProvider and PrayerTimesProvider*/
        unregisterReceiver(mEdgeSinglePlusProvider)

        /* Stop scheduling prayer updates */
        mHandler.removeCallbacks(mDailyRunnable)
    }

    private fun updateWidget() {
        mCocktailIds = SlookCocktailManager.getInstance(this).getCocktailIds(
                ComponentName(this, EdgeSinglePlusProvider::class.java)
        )

        setPrayerTimes(packageName, mCocktailIds, this)
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
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

}