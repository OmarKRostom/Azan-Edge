package com.omarkrostom.azanEdge.oldApp.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import com.omarkrostom.azanEdge.oldApp.Constants.ASR_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.DEFAULT_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.FAJR_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.ISHA_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.MAGHRIB_ADJUSTMENT
import com.omarkrostom.azanEdge.oldApp.Constants.ZUHR_ADJUSTMENT
import com.omarkrostom.azanEdge.R
import com.omarkrostom.azanEdge.oldApp.utils.get
import com.omarkrostom.azanEdge.oldApp.utils.set
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.activity_manual_adjustments.*

class ManualTimesActivity : BaseActivity(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_adjustments)
    }

    override fun onStart() {
        super.onStart()
        initializePreferenceManager()
        setOnValueChangeListeners()
        initializeSlookInstance()
        initializeActionbar()
        setManualTimeAdjustments()
    }

    private fun setManualTimeAdjustments() {
        np_fajr_prayer.value = mPreferenceManager.get(FAJR_ADJUSTMENT, DEFAULT_ADJUSTMENT) as Int
        np_zuhr_prayer.value = mPreferenceManager.get(ZUHR_ADJUSTMENT, DEFAULT_ADJUSTMENT) as Int
        np_asr_prayer.value = mPreferenceManager.get(ASR_ADJUSTMENT, DEFAULT_ADJUSTMENT) as Int
        np_maghreb_prayer.value = mPreferenceManager.get(MAGHRIB_ADJUSTMENT, DEFAULT_ADJUSTMENT) as Int
        np_isha_prayer.value = mPreferenceManager.get(ISHA_ADJUSTMENT, DEFAULT_ADJUSTMENT) as Int
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initializeActionbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setOnValueChangeListeners() {
        np_fajr_prayer.setOnValueChangedListener(this::onPrayerTimeAdjusted)
        np_zuhr_prayer.setOnValueChangedListener(this::onPrayerTimeAdjusted)
        np_asr_prayer.setOnValueChangedListener(this::onPrayerTimeAdjusted)
        np_maghreb_prayer.setOnValueChangedListener(this::onPrayerTimeAdjusted)
        np_isha_prayer.setOnValueChangedListener(this::onPrayerTimeAdjusted)
    }

    private fun onPrayerTimeAdjusted(picker: NumberPicker, oldVal: Int, newVal: Int) {
        when (picker.id) {
            R.id.np_fajr_prayer ->
                mPreferenceManager.set(FAJR_ADJUSTMENT, newVal)
            R.id.np_zuhr_prayer ->
                mPreferenceManager.set(ZUHR_ADJUSTMENT, newVal)
            R.id.np_asr_prayer ->
                mPreferenceManager.set(ASR_ADJUSTMENT, newVal)
            R.id.np_maghreb_prayer ->
                mPreferenceManager.set(MAGHRIB_ADJUSTMENT, newVal)
            R.id.np_isha_prayer ->
                mPreferenceManager.set(ISHA_ADJUSTMENT, newVal)
        }
    }

}