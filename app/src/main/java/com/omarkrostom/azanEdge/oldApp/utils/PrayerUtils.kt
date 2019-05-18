package com.omarkrostom.azanEdge.oldApp.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.widget.RemoteViews
import com.omarkrostom.azanEdge.oldApp.Constants
import com.omarkrostom.azanEdge.oldApp.Constants.ASR_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.DEFAULT_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.FAJR_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.ISHA_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.MAGHRIB_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.TWELVE_HOUR_FORMAT
import com.omarkrostom.azanEdge.oldApp.Constants.TWENTY_FOUR_HOUR_FORMAT
import com.omarkrostom.azanEdge.oldApp.Constants.ZUHR_ADJUSTMENT
import com.omarkrostom.azanEdge.R
import com.omarkrostom.azanEdge.oldApp.activities.SettingsActivity
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*
import android.net.ConnectivityManager


fun setPrayerTimes(packageName: String,
                   cocktailIds: IntArray,
                   layoutId: Int,
                   context: Context,
                   isSamsungSdk: Boolean) {
    val remoteViews = RemoteViews(packageName, layoutId)
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

    if (isSamsungSdk) {
        SlookCocktailManager.getInstance(context).updateCocktail(cocktailIds[0], remoteViews, helpRemoteViews)
    } else {
        val intent = Intent(context, SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        remoteViews.setOnClickPendingIntent(R.id.ll_main_app, pendingIntent)
        AppWidgetManager.getInstance(context).updateAppWidget(cocktailIds[0], remoteViews)
    }

}

fun getAzanTiming(mPreferenceManager: SharedPreferences, prayer: String): CharSequence? {
    return when (mPreferenceManager.get(Constants.HOUR_MODE, Constants.TWENTY_FOUR_HOUR)) {
        Constants.TWELVE_HOUR ->
            appendWithManualTimeAdjustments(
                    mPreferenceManager,
                    TWELVE_HOUR_FORMAT,
                    prayer,
                    convertToTimeFormat(mPreferenceManager.get(prayer, Constants.PRAYER_DEFAULT).toString())
            )
        else ->
            appendWithManualTimeAdjustments(
                    mPreferenceManager,
                    TWENTY_FOUR_HOUR_FORMAT,
                    prayer,
                    mPreferenceManager.get(prayer, Constants.PRAYER_DEFAULT).toString())
    }
}

fun appendWithManualTimeAdjustments(mPreferenceManager: SharedPreferences,
                                    prayerFormat: String,
                                    prayerTitle: String,
                                    prayerTime: String): CharSequence {
    val adjustmentRequired = when (prayerTitle) {
        Constants.FAJR_PRAYER -> mPreferenceManager.get(FAJR_ADJUSTMENT, DEFAULT_ADJUSTMENT)
        Constants.ZUHR_PRAYER -> mPreferenceManager.get(ZUHR_ADJUSTMENT, DEFAULT_ADJUSTMENT)
        Constants.ASR_PRAYER -> mPreferenceManager.get(ASR_ADJUSTMENT, DEFAULT_ADJUSTMENT)
        Constants.MAGHRIB_PRAYER -> mPreferenceManager.get(MAGHRIB_ADJUSTMENT, DEFAULT_ADJUSTMENT)
        Constants.ISHA_PRAYER -> mPreferenceManager.get(ISHA_ADJUSTMENT, DEFAULT_ADJUSTMENT)
        else -> {
            throw UnknownError("Undefined prayer format !")
        }
    }
    val currentTimeFormat = SimpleDateFormat(prayerFormat, Locale.getDefault())
    val prayerTimeParsed = currentTimeFormat.parse(prayerTime).time
    val prayerTimeAdjusted = prayerTimeParsed + (adjustmentRequired as Int * 60000)
    return currentTimeFormat.format(prayerTimeAdjusted)
}

fun convertToTimeFormat(prayerTime: String): String {
    val currentTimeFormat = SimpleDateFormat(TWENTY_FOUR_HOUR_FORMAT, Locale.getDefault())
    val desiredTimeFormat = SimpleDateFormat(TWELVE_HOUR_FORMAT, Locale.getDefault())
    val outPutPrayerTime = currentTimeFormat.parse(prayerTime)
    return desiredTimeFormat.format(outPutPrayerTime)
}

fun isInternetConnected(): Boolean {
    return try {
        val ipAddress = InetAddress.getByName("www.google.com")
        ipAddress.address.toString() != ""
    } catch (e: Exception) {
        false
    }
}

fun isInternetConnectedFromActivity(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}