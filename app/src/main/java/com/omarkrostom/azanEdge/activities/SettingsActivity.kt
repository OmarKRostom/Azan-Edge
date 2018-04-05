package com.omarkrostom.azanEdge.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.location.*
import com.omarkrostom.azanEdge.Config
import com.omarkrostom.azanEdge.Constants
import com.omarkrostom.azanEdge.broadcastReceivers.EdgeSinglePlusProvider
import com.omarkrostom.azanEdge.networking.PrayersApiManager
import com.omarkrostom.azanEdge.networking.models.PrayerResponse
import com.omarkrostom.azanEdge.utils.PermissionDispatcher
import com.omarkrostom.azanEdge.utils.get
import com.omarkrostom.azanEdge.utils.set
import com.omarkrostom.azanEdge.utils.setPrayerTimes
import com.omarkrostom.azanEdge.R
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : AppCompatActivity(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        PrayersApiManager.OnPrayerTimesApiListener {

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocation: Location
    private lateinit var mPreferenceManager: SharedPreferences
    private lateinit var mCocktailIds: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onStart() {
        super.onStart()
        createLocationRequestInstance()
        createFusedLocationProviderInstance()
        createLocationCallbackInstance()
        setActionListeners()
        setSharedPreferencesInstance()
        updateLocationInButton()
        updateMethodInButton()
        updateHourModeSwitch()
    }

    private fun setActionListeners() {
        fl_location_btn.setOnClickListener(this::onChooseLocationClicked)
        fl_azan_method_btn.setOnClickListener(this::onChooseAzanMethodClicked)
        sc_time_format.setOnCheckedChangeListener(this::onPrayerTimingChanged)
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

                    /* Show that method updated sucessfully */
                    Toast.makeText(this,
                            getString(R.string.method_updated_successfully),
                            Toast.LENGTH_LONG).show()
                }
                .positiveText(android.R.string.ok)
                .show()
    }

    private fun getSelectedIndexFromPreference(): Int {
        return mPreferenceManager.get(Constants.AZAN_METHOD_INDEX, Constants.DEFAULT_METHOD_INDEX) as Int
    }

    private fun onChooseLocationClicked(view: View) {
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

    private fun setSharedPreferencesInstance() {
        mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        mPreferenceManager.registerOnSharedPreferenceChangeListener(this)
    }

    private fun setLocationToPreferences() {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val addresses = geoCoder.getFromLocation(mLocation.latitude, mLocation.longitude, 1)
        mPreferenceManager.set(Constants.APP_LAT, mLocation.latitude.toLong())
        mPreferenceManager.set(Constants.APP_LONG, mLocation.longitude.toLong())
        mPreferenceManager.set(Constants.APP_CITY, addresses[0].adminArea)
        mPreferenceManager.set(Constants.APP_COUNTRY, addresses[0].countryName)

        updateLocationInButton()

        /* Show that location updated sucessfully */
        Toast.makeText(this,
                getString(R.string.location_updated_successfully),
                Toast.LENGTH_LONG).show()
    }

    private fun updateLocationInButton() {
        tv_location_result.text = "${
        mPreferenceManager.get(Constants.APP_COUNTRY, Constants.DEFAULT_COUNTRY)
        }, ${
        mPreferenceManager.get(Constants.APP_CITY, Constants.DEFAULT_CITY)
        }"
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
                sharedPreferences.get(Constants.APP_COUNTRY, Constants.DEFAULT_COUNTRY).toString(),
                sharedPreferences.get(Constants.APP_CITY, Constants.DEFAULT_CITY).toString(),
                ((sharedPreferences.get(Constants.AZAN_METHOD_INDEX, Constants.DEFAULT_METHOD_INDEX) as Int) + 1).toString(),
                this::onSuccess,
                this::onError
        )
    }

    private fun updateWidget() {
        mCocktailIds = SlookCocktailManager.getInstance(this).getCocktailIds(
                ComponentName(this, EdgeSinglePlusProvider::class.java)
        )

        setPrayerTimes(packageName, mCocktailIds, this)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            Config.LOCATION_PERMISSION ->
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocationUpdates()
                } else {
                    Toast.makeText(this, getString(R.string.please_allow_location), Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        getPrayerTimesFromApi(sharedPreferences)
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