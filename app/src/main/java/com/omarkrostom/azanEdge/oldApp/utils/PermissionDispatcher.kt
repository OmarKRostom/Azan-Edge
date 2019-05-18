package com.omarkrostom.azanEdge.oldApp.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.location.LocationManager
import com.omarkrostom.azanEdge.oldApp.Config


/**
 * Created by omarkrostom on 1/4/18.
 */
object PermissionDispatcher {

    fun checkLocationPermission(context: Context): Boolean {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
            return false
        return true

    }

    fun requestLocationPermission(activity: Activity) {

        ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Config.LOCATION_PERMISSION
        )

    }

    fun checkLocationEnabled(context: Context): Boolean =
            (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)

}