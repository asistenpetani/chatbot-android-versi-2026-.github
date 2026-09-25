package com.asistenpetani.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionManager {

    /**
     * List of essential permissions for the ASPEK application
     */
    val ESSENTIAL_PERMISSIONS: Array<String>
        get() {
            val list = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            return list.toTypedArray()
        }

    /**
     * Check if a single permission is granted
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        val appContext = context.applicationContext ?: context
        return ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if all essential permissions are granted
     */
    fun hasAllEssentialPermissions(context: Context): Boolean {
        return ESSENTIAL_PERMISSIONS.all { isPermissionGranted(context, it) }
    }

    /**
     * Get a list of ungranted permissions that need to be requested
     */
    fun getUngrantedPermissions(context: Context): List<String> {
        return ESSENTIAL_PERMISSIONS.filter { !isPermissionGranted(context, it) }
    }
}

/**
 * A handy Compose Side-Effect composable to request permissions seamlessly
 */
@Composable
fun RequestEssentialPermissionsEffect(
    onResult: (Map<String, Boolean>) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = onResult
    )

    LaunchedEffect(Unit) {
        launcher.launch(PermissionManager.ESSENTIAL_PERMISSIONS)
    }
}
