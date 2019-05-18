package com.omarkrostom.azanEdge.oldApp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.location.*
import com.omarkrostom.azanEdge.oldApp.Config
import com.omarkrostom.azanEdge.oldApp.Constants
import com.omarkrostom.azanEdge.R
import com.omarkrostom.azanEdge.oldApp.networking.models.PrayerResponse
import com.omarkrostom.azanEdge.oldApp.networking.models.ReverseGeocodedObjectResponse
import com.omarkrostom.azanEdge.oldApp.networking.networkManagers.PrayersApiManager
import com.omarkrostom.azanEdge.oldApp.networking.networkManagers.PrayersApiManager.getLocationFormattedAddress
import com.omarkrostom.azanEdge.oldApp.utils.PermissionDispatcher
import com.omarkrostom.azanEdge.oldApp.utils.get
import com.omarkrostom.azanEdge.oldApp.utils.getAzanTiming
import com.omarkrostom.azanEdge.oldApp.utils.set
import com.omarkrostom.azanEdge.utils.*
import com.samsung.android.sdk.look.Slook
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : BaseActivity(),
        PrayersApiManager.OnPrayerTimesApiListener, PrayersApiManager.OnFormattedLocationApiListener {

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocation: Location

    private var mAppWidgetId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onStart() {
        super.onStart()
        initializeSlookInstance()
        setCurrentWidgetId()
        createLocationRequestInstance()
        createFusedLocationProviderInstance()
        createLocationCallbackInstance()
        setActionListeners()
        initializePreferenceManager()
        updateLocationInButton()
        updateMethodInButton()
        updateHourModeSwitch()
        updatePreviewTimes()
        onChooseLocationClicked()
    }

    override fun onSuccess(prayerResponse: PrayerResponse) {
        /* Save response to preferences */
        mPreferenceManager.set(Constants.FAJR_PRAYER, prayerResponse.data.prayerTimings.fajr)
        mPreferenceManager.set(Constants.ZUHR_PRAYER, prayerResponse.data.prayerTimings.zuhr)
        mPreferenceManager.set(Constants.ASR_PRAYER, prayerResponse.data.prayerTimings.asr)
        mPreferenceManager.set(Constants.MAGHRIB_PRAYER, prayerResponse.data.prayerTimings.maghrib)
        mPreferenceManager.set(Constants.ISHA_PRAYER, prayerResponse.data.prayerTimings.isha)

        /* Set Hijri Date */
        tv_day.visibility = View.VISIBLE
        tv_day.text = prayerResponse.data.prayersDate.hijriDate
                .get("day").toString().replace("\"", "")

        tv_month.visibility = View.VISIBLE
        tv_month.text = prayerResponse.data.prayersDate.hijriDate
                .getAsJsonObject("month").get("en").toString().replace("\"", "")

        tv_year.visibility = View.VISIBLE
        tv_year.text = prayerResponse.data.prayersDate.hijriDate
                .get("year").toString().replace("\"", "")

        updateWidgets()
        updatePreviewTimes()
    }

    override fun onError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    override fun onLocationSetSuccess(reverseGeocodedObjectResponse: ReverseGeocodedObjectResponse) {
        mPreferenceManager.set(Constants.APP_ADDRESS, reverseGeocodedObjectResponse
                .addressComponents[0].formattedAddress)
        mPreferenceManager.set(Constants.APP_LAT, mLocation.latitude.toString())
        mPreferenceManager.set(Constants.APP_LONG, mLocation.longitude.toString())

        updateLocationInButton()
    }

    override fun onLocationSetError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.btn_save -> finishAndSave()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setCurrentWidgetId() {
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
    }

    private fun setActionListeners() {
        ll_location_btn.setOnClickListener(this::onChooseLocationClicked)
        ll_azan_method_btn.setOnClickListener(this::onChooseAzanMethodClicked)
        sc_time_format.setOnCheckedChangeListener(this::onPrayerTimingChanged)
        ll_manual_adjustments_title.setOnClickListener(this::onManualAdjustmentsClicked)
        ll_samsung_enabled.setOnClickListener(this::onSamsungCompatibleClicked)
    }

    private fun onManualAdjustmentsClicked(view: View) {
        val intent = Intent(this, ManualTimesActivity::class.java)
        startActivity(intent)
    }

    private fun onSamsungCompatibleClicked(view: View) {
        val message: String = if (mSlookInstance.isFeatureEnabled(Slook.COCKTAIL_PANEL)) {
            getString(R.string.samsung_edge_panel_enabled)
        } else {
            getString(R.string.samsung_edge_panel_disabled)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun onPrayerTimingChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (isChecked) {
            true -> {
                sc_time_format.text = getString(R.string.twelve_hr)
                mPreferenceManager.set(Constants.HOUR_MODE, Constants.TWELVE_HOUR)
            }
            false -> {
                sc_time_format.text = getString(R.string.twenty_four_hr)
                mPreferenceManager.set(Constants.HOUR_MODE, Constants.TWENTY_FOUR_HOUR)
            }
        }

        updateWidgets()
        updatePreviewTimes()
    }

    private fun onChooseAzanMethodClicked(view: View) {
        MaterialDialog.Builder(this)
                .title(R.string.azan_method)
                .items(R.array.azan_methods)
                .itemsCallbackSingleChoice(getSelectedIndexFromPreference(), { _, _, _, _ ->
                    return@itemsCallbackSingleChoice true
                })
                .onPositive { dialog, _ ->
                    mPreferenceManager.set(Constants.AZAN_METHOD,
                            resources.getStringArray(R.array.azan_methods)[dialog.selectedIndex])
                    mPreferenceManager.set(Constants.AZAN_METHOD_INDEX,
                            dialog.selectedIndex)

                    updateMethodInButton()
                }
                .positiveText(android.R.string.ok)
                .show()
    }

    private fun getSelectedIndexFromPreference(): Int {
        return mPreferenceManager.get(Constants.AZAN_METHOD_INDEX, Constants.DEFAULT_METHOD_INDEX) as Int
    }

    private fun onChooseLocationClicked(view: View = View(this)) {
        if (PermissionDispatcher.checkLocationPermission(this)) {
            if (PermissionDispatcher.checkLocationEnabled(this)) {
                getLocationUpdates()
                pb_loading.visibility = View.VISIBLE
            } else {
                Toast.makeText(this,
                        getString(R.string.please_allow_location),
                        Toast.LENGTH_LONG).show()
            }
        } else {
            PermissionDispatcher.requestLocationPermission(this)
        }
    }

    private fun updateHourModeSwitch() {
        when (mPreferenceManager.get(Constants.HOUR_MODE, Constants.TWENTY_FOUR_HOUR)) {
            Constants.TWELVE_HOUR -> {
                sc_time_format.isChecked = true
                sc_time_format.text = getString(R.string.twelve_hr)
            }
            Constants.TWENTY_FOUR_HOUR -> {
                sc_time_format.isChecked = false
                sc_time_format.text = getString(R.string.twenty_four_hr)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                Looper.myLooper())
    }

    private fun setLocationToPreferences() {
        getLocationFormattedAddress(
                mLocation.longitude.toString(),
                mLocation.latitude.toString(),
                this::onLocationSetSuccess,
                this::onLocationSetError
        )
    }

    private fun updateLocationInButton() {
        tv_location_result.text = "${mPreferenceManager.get(Constants.APP_ADDRESS,
                Constants.DEFAULT_ADDRESS)}"
    }

    private fun updateMethodInButton() {
        tv_azan_method_result.text =
                mPreferenceManager.get(Constants.AZAN_METHOD, Constants.DEFAULT_METHOD).toString()
    }

    private fun createLocationCallbackInstance() {
        mLocationCallback = object : LocationCallback() {
            /**
             * Handle on location result
             */
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    mLocation = location
                }
                setLocationToPreferences()
                pb_loading.visibility = View.GONE
                mFusedLocationProviderClient.removeLocationUpdates(this)
            }

            /**
             * Check location availability
             */
            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                when (locationAvailability.isLocationAvailable) {

                }
            }
        }
    }

    private fun createLocationRequestInstance() {
        mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = Config.LOCATION_INTERVAL
        mLocationRequest.fastestInterval = Config.FASTEST_LOCATION_INTERVAL
    }

    private fun createFusedLocationProviderInstance() {
        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this)
    }

    private fun getPrayerTimesFromApi(sharedPreferences: SharedPreferences = mPreferenceManager) {
        PrayersApiManager.getPrayerTimesFromApi(
                sharedPreferences.get(Constants.APP_LAT, Constants.DEFAULT_LAT).toString(),
                sharedPreferences.get(Constants.APP_LONG, Constants.DEFAULT_LONG).toString(),
                ((sharedPreferences.get(Constants.AZAN_METHOD_INDEX, Constants.DEFAULT_METHOD_INDEX) as Int) + 1).toString(),
                this::onSuccess,
                this::onError
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            Config.LOCATION_PERMISSION ->
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onChooseLocationClicked()
                } else {
                    showPermissionDialog()
                }
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.permissions_disabled))
                .setMessage(getString(R.string.permissions_disabled_string))
                .setPositiveButton(R.string.go_to_settings, { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", packageName, null))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                })
                .show()
    }

    private fun updatePreviewTimes() {
        tv_fajr_prayer.text = getAzanTiming(mPreferenceManager, Constants.FAJR_PRAYER)
        tv_zuhr_prayer.text = getAzanTiming(mPreferenceManager, Constants.ZUHR_PRAYER)
        tv_asr_prayer.text = getAzanTiming(mPreferenceManager, Constants.ASR_PRAYER)
        tv_maghreb_prayer.text = getAzanTiming(mPreferenceManager, Constants.MAGHRIB_PRAYER)
        tv_isha_prayer.text = getAzanTiming(mPreferenceManager, Constants.ISHA_PRAYER)
    }

    private fun finishAndSave(): Boolean {
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
        return true
    }

}