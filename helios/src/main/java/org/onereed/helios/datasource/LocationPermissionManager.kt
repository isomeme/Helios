package org.onereed.helios.datasource

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.onereed.helios.R
import timber.log.Timber

class LocationPermissionManager(private val activity: ComponentActivity) {

  private val requestPermissionLauncher =
    activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      acceptLocationPermissionResult(isGranted)
    }

  init {
    activity.lifecycle.addObserver(
      object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
          maybeRequestLocationPermission()
        }
      }
    )
  }

  private fun maybeRequestLocationPermission() {
    if (activity.checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
      Timber.d("Location permission already granted.")
    } else {
      Timber.d("Requesting location permission.")
      requestLocationPermission()
    }
  }

  private fun requestLocationPermission() = requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)

  private fun acceptLocationPermissionResult(isGranted: Boolean) {
    Timber.d("acceptLocationPermissionResult: isGranted=$isGranted")

    if (isGranted) {
      return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      activity.window.decorView.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    if (activity.shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
      AlertDialog.Builder(activity)
        .setMessage(R.string.location_permission_rationale)
        .setPositiveButton(R.string.button_continue) { _, _ -> requestLocationPermission() }
        .setNegativeButton(R.string.button_exit) { _, _ -> activity.finish() }
        .setCancelable(false)
        .create()
        .show()

      Timber.d("Launched rationale dialog.")
    } else {
      val settingsIntent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
          .setData(Uri.fromParts("package", activity.packageName, /* fragment= */ null))

      AlertDialog.Builder(activity)
        .setMessage(R.string.location_permission_use_settings)
        .setPositiveButton(R.string.button_settings) { _, _ ->
          activity.startActivity(settingsIntent)
        }
        .setNegativeButton(R.string.button_exit) { _, _ -> activity.finish() }
        .setCancelable(false)
        .create()
        .show()

      Timber.d("Launched use-settings dialog.")
    }
  }

  companion object {

    fun ComponentActivity.startLocationPermissionManager() = LocationPermissionManager(this)
  }
}
