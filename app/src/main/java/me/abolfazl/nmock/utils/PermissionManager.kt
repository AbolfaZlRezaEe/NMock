package me.abolfazl.nmock.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat

object PermissionManager {

    fun checkingPermissions(@NonNull context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            (checkPermissionIsGranted(Manifest.permission.ACCESS_COARSE_LOCATION, context)
                    && checkPermissionIsGranted(Manifest.permission.ACCESS_FINE_LOCATION, context)
                    && checkPermissionIsGranted(Manifest.permission.FOREGROUND_SERVICE, context))
        } else {
            (checkPermissionIsGranted(Manifest.permission.ACCESS_COARSE_LOCATION, context)
                    && checkPermissionIsGranted(Manifest.permission.ACCESS_FINE_LOCATION, context))
        }
    }

    private fun checkPermissionIsGranted(
        permission: String,
        @NonNull context: Context
    ): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            context,
            permission
        )
    }

    fun getPermissionList(): List<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // for using foregroundService in our app:
            permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }
        return permissions
    }

    fun locationIsEnabled(@NonNull context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}