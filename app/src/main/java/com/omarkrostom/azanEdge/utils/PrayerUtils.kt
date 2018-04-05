package com.omarkrostom.azanEdge.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.widget.RemoteViews
import com.omarkrostom.azanEdge.Constants
import com.omarkrostom.azanEdge.R
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager
import java.text.SimpleDateFormat
import java.util.*

fun setPrayerTimes(packageName: String,
                   cocktailIds: IntArray,
                   context: Context) {
    val remoteViews = RemoteViews(packageName, R.layout.layout_main)
    val helpRemoteViews = RemoteViews(packageName, R.layout.layout_help)
    val mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(context)

    remoteViews.setTextViewText(R.id.tv_fajr_prayer,
            getAzanTiming(mPreferenceManager, Constants.FAJR_PRAYER))

    remoteViews.setTextViewText(R.id.tv_zuhr_prayer,
            getAzanTiming(mPreferenceManager, Constants.ZUHR_PRAYER))

    remoteViews.setTextViewText(R.id.tv_asr_prayer,
            getAzanTiming(mPreferenceManager, Constants.ASR_PRAYER))

    remoteViews.setTextViewText(R.id.tv_maghreb_prayer,
            getAzanTiming(mPreferenceManager, Constants.MAGHRIB_PRAYER))

    remoteViews.setTextViewText(R.id.tv_isha_prayer,
            getAzanTiming(mPreferenceManager, Constants.ISHA_PRAYER))

    SlookCocktailManager.getInstance(context).updateCocktail(cocktailIds[0], remoteViews, helpRemoteViews)

}

fun getAzanTiming(mPreferenceManager: SharedPreferences, prayer: String): CharSequence? {
    return when (mPreferenceManager.get(Constants.HOUR_MODE, Constants.TWENTY_FOUR_HOUR)) {
        Constants.TWELVE_HOUR -> convertToTwelveHrFormat(mPreferenceManager.get(prayer, Constants.PRAYER_DEFAULT).toString())
        else -> mPreferenceManager.get(prayer, Constants.PRAYER_DEFAULT).toString()
    }
}

fun convertToTwelveHrFormat(prayerTwentyFourHrFormat: String): CharSequence? {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    val twelveHrDate = timeFormat.parse(prayerTwentyFourHrFormat)
    return SimpleDateFormat("hh:mm a").format(twelveHrDate)
}
