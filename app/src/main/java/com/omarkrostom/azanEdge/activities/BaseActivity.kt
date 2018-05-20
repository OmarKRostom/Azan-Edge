package com.omarkrostom.azanEdge.activities

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.omarkrostom.azanEdge.R
import com.omarkrostom.azanEdge.utils.setPrayerTimes
import com.omarkrostom.azanEdge.widgetProviders.AzanEdgeHorizontalProvider
import com.omarkrostom.azanEdge.widgetProviders.AzanEdgeSinglePlusProvider
import com.samsung.android.sdk.look.Slook
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager

open class BaseActivity: AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    lateinit var mPreferenceManager: SharedPreferences
    lateinit var mSlookInstance: Slook

    var mCocktailIds: IntArray? = null
    var mAppHorizontalWidgetIds: IntArray? = null

    fun initializeSlookInstance() {
        mSlookInstance = Slook()
    }

    fun initializePreferenceManager() {
        mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        mPreferenceManager.registerOnSharedPreferenceChangeListener(this)
    }

    fun updateWidgets() {
        updateSamsungWidget()
        updateHorizontalWidget()
    }

    private fun updateSamsungWidget() {
        mCocktailIds = SlookCocktailManager.getInstance(this).getCocktailIds(
                ComponentName(this, AzanEdgeSinglePlusProvider::class.java)
        )

        if (mCocktailIds?.isNotEmpty()!!
                && mSlookInstance.isFeatureEnabled(Slook.COCKTAIL_PANEL)) {
            setPrayerTimes(packageName,
                    mCocktailIds!!,
                    R.layout.layout_main_vertical,
                    this,
                    true)
        }
    }

    private fun updateHorizontalWidget() {
        mAppHorizontalWidgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(
                ComponentName(this, AzanEdgeHorizontalProvider::class.java)
        )

        if (mAppHorizontalWidgetIds?.isNotEmpty()!!) {
            setPrayerTimes(packageName,
                    mAppHorizontalWidgetIds!!,
                    R.layout.layout_main_horizontal,
                    this,
                    false)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        updateWidgets()
    }

}