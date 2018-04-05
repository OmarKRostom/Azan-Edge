package com.omarkrostom.azanEdge.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.app.ActivityManager
import android.location.LocationManager
import com.omarkrostom.azanEdge.Config


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

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any {
            serviceClass.name == it.service.className
        }
    }

    fun checkLocationEnabled(context: Context): Boolean =
            (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)

}